package com.mammon.documntdb.node;

import com.github.dockerjava.api.model.Container;

import java.io.IOException;
import java.util.List;

public interface Docker {
    String createDockerContainer(int localHostPort, String name) throws IOException;

    void startContainer(String containerName);

    void stopContainer(String containerName);

    void killContainer(String containerName);

    void removeContainer(String containerName);

    List<Container> listContainers(String databaseName, String collectionName);

}
