package com.mammon.documntdb.node;

import com.github.dockerjava.api.model.Container;
import com.mammon.documntdb.authentication.SHA512;
import com.mammon.documntdb.database.*;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Scope("singleton")
public class NodeManagerV2 implements ReadNode {
    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private static Map<String, String> nodesIP = new HashMap<>();
    private Docker docker = DockerUtil.createDockerEngine();
    private NodeUtil nodeUtil = new NodeUtil();
    private RestTemplate restTemplate = new RestTemplate();

    private NodeManagerV2() {
    }

    public static NodeManagerV2 createReadNodeManager() {
        return new NodeManagerV2();
    }


    @Override
    public void createNodes(String databaseName, String collectionName, int numberOfInstance) throws IOException {

        for (int i = 0; i < numberOfInstance; i++) {
            createNode(databaseName, collectionName);
        }
    }

    @Override
    public void createNode(String databaseName, String collectionName) throws IOException {
        String nodeName = nodeUtil.createName(databaseName, collectionName);
        ServerSocket s = new ServerSocket(0);
        int portNumber = s.getLocalPort();
        s.close();
        try {
            String ipAddress = docker.createDockerContainer(portNumber, nodeName);
            synchronized (this) {
                nodesIP.put(nodeName, ipAddress);
            }
            nodeUtil.storeNodeData(nodeName, portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeNodes(String databaseName, String collectionName) throws IOException {
        List<Container> containers = docker.listContainers(databaseName, collectionName);
        for (Container container : containers) {
            docker.removeContainer(container.getId());
        }
        nodeUtil.removeNodes(databaseName, collectionName);
    }

    @Override
    public void updateNode(String databaseName, String collectionName, DocumentCollection collection) throws InterruptedException, NoSuchAlgorithmException {
        try {
            int numberOfInstance = nodeUtil.getNumberOfInstance(databaseName, collectionName);
            String nodeName = databaseName + "-" + collectionName;
            for (int instanceNumber = 1; instanceNumber <= numberOfInstance; instanceNumber++) {
                Optional<Integer> port = nodeUtil.getPort(nodeName + "-" + instanceNumber);//getPort(databaseName, collectionName);
                if (!port.isPresent())
                    continue;
                if (!checkNodeResponse(nodeName, instanceNumber, port))
                    continue;

                updateNodeRestTemplate(nodeName, instanceNumber, collectionName, collection, port);
            }
        } catch (ResourceAccessException e) {
            System.out.println("cant update the node");
        }
    }

    private void updateNodeRestTemplate(String nodeName, int instanceNumber, String collectionName, DocumentCollection collection, Optional<Integer> port) throws NoSuchAlgorithmException {
        String uri = "http://" + nodesIP.get(nodeName + "-" + instanceNumber) + ":" + 8090;
        String nodeUri = uri + "/collection/"
                + SHA512.toHexString(SHA512.getSHA(collectionName));
        System.out.println(nodeUri);
        HttpEntity<DocumentCollection> request = new HttpEntity<>(collection);
        restTemplate.postForObject(nodeUri, request, DocumentCollection.class);
    }

    private boolean checkNodeResponse(String nodeName, int instanceNumber, Optional<Integer> port) {
        String uri = "http://" + nodesIP.get(nodeName + "-" + instanceNumber) + ":" + 8090;
        int numberOfPings = 0;
        while (!pingNode(uri + "/ping")) {
            numberOfPings++;
            if (numberOfPings == 10)
                break;
        }
        if (numberOfPings == 10)
            return false;
        return true;
    }

    private boolean pingNode(String nodeUri) {
        boolean working = false;
        try {
            working = restTemplate.getForObject(nodeUri, boolean.class);
        } catch (ResourceAccessException e) {
            System.out.println("cant ping the node, please wait\n" + nodeUri);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            return false;
        }
        return working;
    }

    @Override
    public Optional<Integer> getPort(String databaseName, String collectionName) {
        synchronized (this) {
            return nodeUtil.getPortRobinBalancing(databaseName, collectionName);
        }
    }

    @Override
    public void destroyAllNodes() throws IOException {
        for (String nodeName : nodeUtil.getAllNodes()) {
            docker.stopContainer(nodeName);
            docker.removeContainer(nodeName);
        }
    }

    @Override
    public void startAllNodes(Map<String, Database> databases) throws IOException, InterruptedException, NoSuchAlgorithmException {
        int initializeTime = 5000;
        int removingTime = 3000;
        deleteOldDatabaseNodes(databases);
        Thread.sleep(removingTime);
        startDatabaseNodes(databases);
        Thread.sleep(initializeTime);
        setDatabaseNodesData(databases);
    }

    private void deleteOldDatabaseNodes(Map<String, Database> databases) throws IOException {
        for (String databaseName : databases.keySet()) {
            if (databaseName.equals("users"))
                continue;
            for (String collectionName : databases.get(databaseName).getCollections().keySet()) {
                removeNodes(databaseName, collectionName);
            }
        }
    }

    private void startDatabaseNodes(Map<String, Database> databases) throws IOException {
        for (String databaseName : databases.keySet()) {
            if (databaseName.equals("users"))
                continue;
            for (String collectionName : databases.get(databaseName).getCollections().keySet()) {
                createNodes(databaseName, collectionName, 2);
            }
        }
    }

    @Override
    public boolean checkIfNodeCreated(String databaseName, String collectionName) {
        return nodeUtil.checkIfNodeCreated(databaseName, collectionName);
    }

    @Override
    public void removeNode(String databaseName, String collectionName) {
        Optional<String> nodeName = nodeUtil.removeNode(databaseName, collectionName);
        if (!nodeName.isPresent())
            return;
        System.out.println(nodeName.get());
        docker.stopContainer(nodeName.get());
        docker.removeContainer(nodeName.get());
        synchronized (this) {
            nodesIP.remove(nodeName);
        }
    }

    private void setDatabaseNodesData(Map<String, Database> databases) throws InterruptedException, NoSuchAlgorithmException {
        for (String databaseName : databases.keySet()) {
            if (databaseName.equals("users"))
                continue;
            for (String collectionName : databases.get(databaseName).getCollections().keySet()) {
                DocumentCollection collection = databases.get(databaseName).getCollection(collectionName).get();
                updateNode(databaseName, collectionName, collection);
            }
        }
    }
}
