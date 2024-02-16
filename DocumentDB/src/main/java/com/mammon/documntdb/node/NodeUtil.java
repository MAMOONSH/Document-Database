package com.mammon.documntdb.node;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NodeUtil {
    private static Map<String, Integer> nodes = new HashMap<>();
    private static Map<String, Integer> nodeInstanceNumber = new HashMap<>();
    private static Map<String, Integer> nodeConnectionIndex = new HashMap<>();
    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    public Set<String> getAllNodes() {
        try {
            lock.readLock().lock();
            return nodes.keySet();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void storeNodeData(String nodeName, Integer port) {
        try {
            lock.writeLock().lock();
            nodes.put(nodeName, port);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String createName(String databaseName, String collectionName) {
        try {
            lock.writeLock().lock();
            String name = databaseName + "-" + collectionName;
            if (nodeInstanceNumber.containsKey(name)) {
                int instanceNumber = nodeInstanceNumber.get(name) + 1;
                nodeInstanceNumber.put(name, instanceNumber);
                return name + "-" + instanceNumber;
            }
            if (!nodeConnectionIndex.containsKey(name))
                nodeConnectionIndex.put(name, 1);
            nodeInstanceNumber.put(name, 1);
            return name + "-" + 1;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeNodes(String databaseName, String collectionName) {
        try {
            lock.writeLock().lock();
            String name = databaseName + "-" + collectionName;
            if (!nodeInstanceNumber.containsKey(name))
                return;
            int numberOfInstances = nodeInstanceNumber.get(name);
            for (int instanceNumber = 1; instanceNumber <= numberOfInstances; instanceNumber++) {
                nodes.remove(name + "-" + instanceNumber);
            }
            nodeConnectionIndex.remove(name);
            nodeInstanceNumber.remove(name);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<String> removeNode(String databaseName, String collectionName) {
        try {
            lock.writeLock().lock();
            String name = databaseName + "-" + collectionName;
            if (!nodeInstanceNumber.containsKey(name))
                return Optional.empty();
            int lastNumber = getNumberOfInstance(databaseName, collectionName);
            if (lastNumber == 1) {
                nodeInstanceNumber.remove(name);
                nodeConnectionIndex.remove(name);
                return Optional.of(name + "-" + 1);
            }
            nodeInstanceNumber.put(name, lastNumber - 1);
            nodes.remove(name + "-" + lastNumber);
            return Optional.of(name + "-" + lastNumber);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int getNumberOfInstance(String databaseName, String collectionName) {
        String name = databaseName + "-" + collectionName;
        if (nodeInstanceNumber.containsKey(name))
            return nodeInstanceNumber.get(name);
        return 0;
    }

    public Optional<Integer> getPortRobinBalancing(String databaseName, String collectionName) {
        try {
            lock.readLock().lock();
            int instanceNumber = getNumberOfInstance(databaseName, collectionName);
            String name = databaseName + "-" + collectionName;
            if (instanceNumber == 0)
                return Optional.empty();
            int index = nodeConnectionIndex.get(name);
            synchronized (this) {
                if (index + 1 > instanceNumber)
                    nodeConnectionIndex.put(name, 1);
                else
                    nodeConnectionIndex.put(name, index + 1);
                return getPort(name + "-" + index);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<Integer> getPort(String nodeName) {
        if (nodes.containsKey(nodeName))
            return Optional.of(nodes.get(nodeName));
        return Optional.empty();
    }

    public Optional<List<String>> getPortsForCollection(String databaseName, String collectionName) {
        int instanceNumber = getNumberOfInstance(databaseName, collectionName);
        if (instanceNumber == 0)
            return Optional.empty();
        List<String> ports = new ArrayList<>();
        String nodeName = databaseName + "-" + collectionName;
        for (int instance = 1; instance <= instanceNumber; instance++) {
            ports.add(getPort(nodeName + "-" + instance) + "");
        }
        return Optional.of(ports);
    }

    public boolean checkIfNodeCreated(String databaseName, String collectionName) {
        return nodeInstanceNumber.containsKey(databaseName + "-" + collectionName);
    }
}
