package com.example.newapp.controll;

import com.example.newapp.model.Website;
import com.example.newapp.model.WebsiteDescription;
//import com.example.newapp.repo.WebsiteDescriptionRepository;
import com.example.newapp.repo.WebsiteRepository;
import com.example.newapp.request.CreateRequest;
import com.example.newapp.response.GetResponse;
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

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
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
    public ResponseEntity<?> createNewWebsite(@RequestParam("name") String name, @RequestParam("url")String url, @RequestParam("file")MultipartFile file){
        Website newWebsite = new Website();
        newWebsite.setWebsite_url(url);
        newWebsite.setWebsite_name(name);
        if (!(getFileName(file).equals(""))) {
            UploadFile(file);
            newWebsite.setSpider_url(root + "/" + getFileName(file));
        }
        Website saveWebsite = webRepo.save(newWebsite);
        return new ResponseEntity<>(saveWebsite, HttpStatus.CREATED);
    }

    @GetMapping("/getName/{id}")
    public ResponseEntity<?> getNameWebsite(@PathVariable("id") Long id){
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()){
            Website getWebsite = otp.get();
            String nameWebSite = getWebsite.getWebsite_name();
            return new ResponseEntity<>(nameWebSite, HttpStatus.OK);
        }
        return new ResponseEntity<>("Id này không tồn tại",HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateWebsite(@PathVariable("id") Long id, @RequestParam("name")String name,@RequestParam("url")String url,@RequestParam("file")MultipartFile file){
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()){
            Website newWebsite = otp.get();
            newWebsite.setWebsite_name(name);
            newWebsite.setWebsite_url(url);
            if (!(getFileName(file).equals(""))){
                UploadFile(file);
                newWebsite.setSpider_url(root + "/"+getFileName(file));
            }
            Website savedWebsite = webRepo.save(newWebsite);
            return new ResponseEntity<>(savedWebsite, HttpStatus.OK);
        }
        return new ResponseEntity<>("update thất bại",HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteWebsite(@PathVariable("id") Long id){
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()){
            Website newWebsite = otp.get();
            webRepo.delete(newWebsite);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>("Xóa thất bại",HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getConfig(@PathVariable("id") Long id){
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()){
            Website newWebsite = otp.get();
            GetResponse res = new GetResponse();
            res.setName(newWebsite.getWebsite_name());
            res.setUrl(newWebsite.getWebsite_url());
            res.setSpide_url(newWebsite.getSpider_url());
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        return new ResponseEntity<>("Không lấy được config" ,HttpStatus.BAD_REQUEST);
    }

    private void UploadFile(MultipartFile file) {
        try {
            if (!(Files.exists(root))){
                Files.createDirectories(root);
            }
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, this.root.resolve(file.getOriginalFilename()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception ex){
            if (ex instanceof FileAlreadyExistsException) {
                throw new RuntimeException("A file of that name already exists.");
            }
        }
    }

    private String getFileName(MultipartFile file){
        String fileName = file.getOriginalFilename();
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
            List<WebsiteDescription> result = new ArrayList<WebsiteDescription>();
            for (WebsiteDescription webDescription : web) {
                int webId = Integer.valueOf(webDescription.getWebsite_id());
                if (webId == id){
                    result.add(webDescription);
                }
            }
            if (result.size() == 0){
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }else{
                return new ResponseEntity<>(web, HttpStatus.OK);
            }
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
