package com.mammon.documntdb.node;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.*;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.context.annotation.Scope;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.github.dockerjava.api.model.HostConfig.newHostConfig;
import static java.util.stream.Collectors.toList;

@Scope("singleton")
public class DockerUtil implements Docker {
    private static final String IMAGE_NAME = "mamoonsh/node-reader-document:2.0";
    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private DockerClientConfig config;
    private DockerHttpClient httpClient;
    private DockerClient dockerClient;
    private int IMAGE_PORT = 8090;

    private DockerUtil() {
        config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();
        dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    public static DockerUtil createDockerEngine() {
        return new DockerUtil();
    }


    @Override
    public String createDockerContainer(int localHostPort, String name) throws IOException {
        ExposedPort tcp = ExposedPort.tcp(IMAGE_PORT);//container port
        Ports portBindings = new Ports();
        portBindings.bind(tcp, Ports.Binding.bindPort(localHostPort));//localhost port
        try {
            lock.writeLock().lock();
            CreateContainerResponse container = dockerClient.createContainerCmd(IMAGE_NAME).withCmd("true")
                    .withName(name)
                    .withExposedPorts(tcp)
                    .withHostConfig(newHostConfig()
                            .withPortBindings(portBindings))
                    .exec();
            dockerClient.startContainerCmd(container.getId()).exec();
            String ipAddress = dockerClient.inspectContainerCmd(container.getId()).exec().getNetworkSettings().getNetworks().get("bridge").getIpAddress();
            return ipAddress;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void startContainer(String containerName) {
        try {
            lock.writeLock().lock();
            dockerClient.startContainerCmd(containerName).exec();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void stopContainer(String containerName) {
        try {
            lock.writeLock().lock();
            dockerClient.stopContainerCmd(containerName).exec();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void killContainer(String containerName) {
        try {
            lock.writeLock().lock();
            dockerClient.killContainerCmd(containerName).exec();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void removeContainer(String containerName) {
        if (containerName != null) {
            try {
                lock.writeLock().lock();
                dockerClient.removeContainerCmd(containerName).withForce(true).exec();
            } catch (Exception ignored) {
                System.out.println("ignored");
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    @Override
    public List<Container> listContainers(String databaseName, String collectionName) {
        try {
            lock.readLock().lock();
            List<Container> containers = dockerClient.listContainersCmd()
                    .withShowAll(true)
                    .withStatusFilter(Arrays.asList("running", "exited", "created"))
                    .exec().stream()
                    .filter(container -> Arrays.stream(container.getNames()).anyMatch(name ->
                            name.startsWith("/" + databaseName + "-" + collectionName)))
                    .collect(toList());
            System.out.println(containers);
            return containers;
        } finally {
            lock.readLock().unlock();
        }
    }
}
