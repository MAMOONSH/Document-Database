package com.mammon.documntdb.fileio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mammon.documntdb.database.Database;
import com.mammon.documntdb.database.Document;
import com.mammon.documntdb.database.DocumentCollection;
import com.mammon.documntdb.database.SchemaObject;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DatabaseIO implements FileOperation {

    String PATH="./database/";
    private DatabaseIO(){}

    public static DatabaseIO createIOOperation(){
        return new DatabaseIO();
    }

    @Override
    public void writeCollection(String databaseName, DocumentCollection collection) throws IOException {
        String schemaName=collection.getSchema().getName();
        String filePath=PATH+databaseName+"/"+schemaName+"/";
        FileUtils.writeStringToFile(new File(
                filePath+"schema.txt"),
                new JSONObject(collection.getSchema().getSchemaJson()).toString(),
                "utf8");
        for(String id:collection.getData().keySet()){
            File file=new File(filePath+"/"+id+".txt");
            if(file.exists())
                continue;
            FileUtils.writeStringToFile(file,new JSONObject(collection.getData().get(id).getData()).toString(),"utf8");
        }
    }
    @Override
    public void updateFile(String databaseName,String schemaName,JSONObject document) throws IOException {
        String filePath=PATH+databaseName+"/"+schemaName+"/"+document.getString("_id")+".txt";
        FileUtils.writeStringToFile(new File(filePath),document.toString(),"utf8");
    }
    @Override
    public void deleteCollection(String databaseName, String collectionName) throws IOException {
        String path=PATH+databaseName+"/"+collectionName;
        FileUtils.forceDelete(new File(path));
    }

    @Override
    public void delete(String databaseName, String schemaName, String id) throws IOException {
        String filePath=PATH+databaseName+"/"+schemaName+"/"+id+".txt";
        File file=new File(filePath);
        if(file.exists())
            FileUtils.forceDelete(file);
    }

    @Override
    public Optional<DocumentCollection> readCollection(String databaseName, String schemaName) throws IOException {
        String filePath=PATH+databaseName+"/"+schemaName+"/";
        File directory=new File(filePath);
        if(directory.exists()) {
            System.out.println(directory.getParent());
            DocumentCollection documentCollection = getDocumentCollection(directory,filePath);
            File[] filesInDirectory=directory.listFiles();
            for(File file:filesInDirectory){
                if(file.getName().equals("schema.txt"))
                    continue;
                //JSONObject jsonData=new JSONObject(FileUtils.readFileToString(file,"utf8"));
                String jsonData=FileUtils.readFileToString(file,"utf8");
                Map<String,Object> fields = new ObjectMapper().readValue(jsonData.toString(), HashMap.class);
                Document document=Document.createDataObject((String) fields.get("_id"),fields);
                documentCollection.addDataObject(document);
            }
            return Optional.of(documentCollection);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Database> readDatabase(String databaseName) throws IOException {
        String filePath=PATH+databaseName+"/";
        File directory=new File(filePath);
        if(directory.exists()) {
            Database database=Database.createDatabase(databaseName);
            File[] filesInDirectory=directory.listFiles();
            for (File file:filesInDirectory){
                String collectionName=file.getName();
                System.out.println(collectionName);
                Optional<DocumentCollection> result=readCollection(databaseName,collectionName);
                if(result.isPresent())
                    database.addCollection(collectionName,result.get());
            }
            return Optional.of(database);
        }
        return Optional.empty();
    }

    @Override
    public Optional<HashMap<String, Database>> readAllDatabases() throws IOException {
        String filePath=PATH+"/";
        File directory=new File(filePath);
        if(directory.exists()) {
            HashMap<String, Database> databases=new HashMap<>();
            File[] filesInDirectory=directory.listFiles();
            for (File file:filesInDirectory){
                String databaseName=file.getName();
                System.out.println(databaseName);
                Optional<Database> result=readDatabase(databaseName);
                if(result.isPresent())
                    databases.put(databaseName,result.get());
            }
            return Optional.of(databases);
        }
        return Optional.empty();
    }

    private DocumentCollection getDocumentCollection(File directory,String filePath) throws IOException {
        File schemaFile=new File(filePath+"schema.txt");
        JSONObject schemaJson=new JSONObject(FileUtils.readFileToString(schemaFile,"utf8"));
        Map<String,Object> fields = new ObjectMapper().readValue(schemaJson.toString(), HashMap.class);
        SchemaObject schemaObject=SchemaObject.createSchema(directory.getName(),fields);
        return DocumentCollection.createCollection(schemaObject);
    }

    @Override
    public File exportDatabase(String databaseName) throws IOException {
        File zipFile;
        String databasePath;
        if(databaseName.toLowerCase()=="database")
            databasePath=PATH;
        else
            databasePath=PATH+databaseName+"/";
        new File("./exported").mkdirs();
        ZipUtil.pack(new File(databasePath),
                zipFile=new File("./exported/"+databaseName+".zip"));
        return zipFile;
    }
    //test the function
    @Override
    public void importDatabase(MultipartFile zipFile) throws IOException {
        String databasePath;
        if(zipFile.getOriginalFilename().toLowerCase()=="database")
            databasePath=PATH;
        else
            databasePath=PATH+zipFile.getOriginalFilename();
        File file=new File(databasePath+".zip");
        file.createNewFile();
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(zipFile.getBytes());
        }
        //zipFile.transferTo(file);
        ZipUtil.unpack(file,new File(databasePath+"/"));
        FileUtils.forceDelete(file);
    }
}
