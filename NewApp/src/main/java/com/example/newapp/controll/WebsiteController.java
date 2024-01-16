package com.example.newapp.controll;

import com.example.newapp.model.Website;
import com.example.newapp.model.WebsiteDescription;
//import com.example.newapp.repo.WebsiteDescriptionRepository;
import com.example.newapp.repo.WebsiteRepository;
import com.example.newapp.request.CreateRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*",maxAge = 3600)
public class WebsiteController {
    @Autowired
    WebsiteRepository webRepo;

    Path root = Paths.get("uploads");

    @GetMapping("/getAll")
    public ResponseEntity<List<Website>> getAllWebsite(){
        List<Website> webList = webRepo.findAll();
        return new ResponseEntity<>(webList,HttpStatus.OK);
    }

    @PostMapping(value="/add-url", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createNewWebsite(@RequestBody CreateRequest request, @RequestParam("file")MultipartFile file){
        Website newWebsite = new Website();
        newWebsite.setWebsite_url(request.getUrl());
        newWebsite.setWebsite_name(request.getName());
        UploadFile(file);
        newWebsite.setSpider_url(root+"/"+getFileName(file));
        Website saveWebsite = webRepo.save(newWebsite);
        return new ResponseEntity<>(saveWebsite, HttpStatus.CREATED);
    }

    @PutMapping("/web/{id}")
    public ResponseEntity<?> updateWebsite(@PathVariable("id") Long id, @RequestBody CreateRequest request,@RequestParam("file")MultipartFile file){
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()){
            Website newWebsite = otp.get();
            newWebsite.setWebsite_name(request.getName());
            newWebsite.setWebsite_url(request.getUrl());
            UploadFile(file);
            newWebsite.setSpider_url(root + "/"+getFileName(file));
            Website savedWebsite = webRepo.save(newWebsite);
            return new ResponseEntity<>(savedWebsite, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteWebsite(@PathVariable("id") Long id){
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()){
            Website newWebsite = otp.get();
            webRepo.delete(newWebsite);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    private void UploadFile(MultipartFile file){
        try{
            Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename()));
        }catch (IOException e){
            throw new RuntimeException("Could not initialize folder for upload!");
        }catch (Exception ex){
            if (ex instanceof FileAlreadyExistsException) {
                throw new RuntimeException("A file of that name already exists.");
            }
            throw new RuntimeException(ex.getMessage());
        }
    }

    private String getFileName(MultipartFile file){
        String fileName = file.getName();
        return fileName;
    }

    @GetMapping("/getAllData/{id}")
    public ResponseEntity<?> getAllDataFromCsv(@PathVariable("id") Long id) {
        String scriptPath = "E:\\NewAppScript\\read_csv.py";
        String csvFilePath = "E:\\NewApp\\data\\output.csv";

        try {
            Process process = Runtime.getRuntime().exec("python " + scriptPath + " " + csvFilePath + " " +id);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder jsonResult = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                jsonResult.append(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Python script exit code: " + exitCode);

            ObjectMapper objectMapper = new ObjectMapper();
            List<WebsiteDescription> web = objectMapper.readValue(jsonResult.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, WebsiteDescription.class));

            return new ResponseEntity<>(web, HttpStatus.OK);

        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
