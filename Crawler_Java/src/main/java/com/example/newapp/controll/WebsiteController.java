package com.example.newapp.controll;

import com.example.newapp.model.User;
import com.example.newapp.model.Website;
import com.example.newapp.model.WebsiteData;
import com.example.newapp.model.WebsiteDescription;
import com.example.newapp.repo.UserRepository;
import com.example.newapp.repo.WebsiteRepository;
import com.example.newapp.request.RegisterRequest;
import com.example.newapp.response.GetNameResponse;
import com.example.newapp.response.GetResponse;
import com.example.newapp.response.ResponseError;
import com.example.newapp.service.MailServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

@RestController
@CrossOrigin(origins = "*",maxAge = 3600)
@Slf4j
public class WebsiteController {
    @Autowired
    WebsiteRepository webRepo;

    Path root = Paths.get("E:\\uploads");

    @Value("${result_json}")
    String json_rs_file_path ;

    @Autowired
    UserRepository repo;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    MailServiceImpl service;

    @PostMapping("/auth/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) throws MessagingException {
        if(repo.existsByEmail(request.getEmail())){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }else{
            Random ran = new Random();
            int minValue = 100000;
            int maxValue = 999999;
            int ranValue  = ran.nextInt((maxValue - minValue) + 1) + minValue;
            User user = new User();
            user.setUser_role(request.getUser_role());
            user.setEmail(request.getEmail());
            user.setUsername(request.getUsername());
            user.setPassword(encoder.encode(String.valueOf(ranValue)));
            User saved_user = repo.save(user);
            sendMail(saved_user.getEmail(), String.valueOf(ranValue));
            return new ResponseEntity<>(saved_user, HttpStatus.CREATED);
        }
    }

    private void sendMail(String email,String pass) throws MessagingException {
        service.SendMail(email, pass);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Website>> getAllWebsite(){
        List<Website> webList = webRepo.findAll();
        for (Website website : webList){
            String filePath = website.getSpider_url();
            filePath = filePath.replace("/","\\");
            Path path = Paths.get(filePath);
            website.setSpider_url(path.getFileName().toString());
        }
        log.info("Get all list website Success : " );
        return new ResponseEntity<>(webList,HttpStatus.OK);
    }

    @PostMapping(value="/add-url", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createNewWebsite(@RequestParam("name") String name, @RequestParam("url")String url, @RequestParam("file")MultipartFile file){
        Website newWebsite = new Website();
        newWebsite.setWebsite_url(url);
        newWebsite.setWebsite_name(name);
        if (!(getFileName(file).equals(""))) {
            ResponseEntity<?> uploadResponse = UploadFile(file,1);
            if (uploadResponse.getStatusCode() == HttpStatus.CONFLICT) {
                log.error("File is conflict name with other file  : " );
                return uploadResponse;
            }
            newWebsite.setSpider_url(root + "/" + getFileName(file));
        }
        Website saveWebsite = webRepo.save(newWebsite);
        log.info("create new website Success : " );
        return new ResponseEntity<>(saveWebsite, HttpStatus.CREATED);
    }

    @GetMapping("/getName/{id}")
    public ResponseEntity<?> getNameWebsite(@PathVariable("id") Long id){
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()){
            Website getWebsite = otp.get();
            GetNameResponse name = new GetNameResponse();
            name.setName(getWebsite.getWebsite_name());
            log.info("Get name website Success : " );
            return new ResponseEntity<>(name, HttpStatus.OK);
        }
        ResponseError error = new ResponseError();
        error.setErrorMessage("Id này không tồn tại");
        log.error("ID not found: " );
        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateWebsite(@PathVariable("id") Long id, @RequestParam("name")String name,@RequestParam("url")String url,@RequestParam("file")MultipartFile file){
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()){
            Website newWebsite = otp.get();
            newWebsite.setWebsite_name(name);
            newWebsite.setWebsite_url(url);
            if (!(getFileName(file).equals(""))){
                ResponseEntity<?> uploadResponse = UploadFile(file,0);
                if (uploadResponse.getStatusCode() == HttpStatus.CONFLICT) {
                    log.error("File is conflict name with other file  : " );
                    return uploadResponse;
                }
                newWebsite.setSpider_url(root + "/" + getFileName(file));
            }
            Website savedWebsite = webRepo.save(newWebsite);
            log.info("Update website Success : " );
            return new ResponseEntity<>(savedWebsite, HttpStatus.OK);
        }
        ResponseError error = new ResponseError();
        log.error("Update website info Fail : " );
        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/update-no-file/{id}")
    public ResponseEntity<?> updateWebsiteNoFile(@PathVariable("id") Long id, @RequestParam("name")String name,@RequestParam("url")String url){
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()){
            Website newWebsite = otp.get();
            newWebsite.setWebsite_name(name);
            newWebsite.setWebsite_url(url);
            Website savedWebsite = webRepo.save(newWebsite);
            log.info("update website with no file Success : " );
            return new ResponseEntity<>(savedWebsite, HttpStatus.OK);
        }
        ResponseError error = new ResponseError();
        error.setErrorMessage("update thất bại");
        log.error("Update website infor with no file Fail : " );
        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteWebsite(@PathVariable("id") Long id){
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()){
            Website newWebsite = otp.get();
            deleteFile(newWebsite.getSpider_url());
            webRepo.delete(newWebsite);
            log.info("Delete website Success : " );
            return new ResponseEntity<>(HttpStatus.OK);
        }
        ResponseError error = new ResponseError();
        error.setErrorMessage("Xóa thất bại");
        log.error("Delete website Fail : " );
        return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getConfig(@PathVariable("id") Long id){
        Optional<Website> otp = webRepo.getWebsiteById(id);
        if (otp.isPresent()){
            Website newWebsite = otp.get();
            GetResponse res = new GetResponse();
            res.setName(newWebsite.getWebsite_name());
            res.setUrl(newWebsite.getWebsite_url());
            String str = newWebsite.getSpider_url();
            String fileName = str.substring(str.lastIndexOf("/") + 1);
            res.setSpider_url(fileName);
            log.info("Open config website Success : " );
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        ResponseError error = new ResponseError();
        error.setErrorMessage("Không lấy được config");
        log.error("Open config website Fail : " );
        return new ResponseEntity<>(error ,HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<?> UploadFile(MultipartFile file,int typeAction) {
        try {
            if (!(Files.exists(root))){
                Files.createDirectories(root);
            }
            Path targetPath = this.root.resolve(file.getOriginalFilename());
            if (typeAction == 1){
                if (Files.exists(targetPath)) {
                    ResponseError error = new ResponseError();
                    error.setErrorMessage("A file of that name already exists.");
                    log.error("Upload file has conflict name with other file : " );
                    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
                }
                try (InputStream in = file.getInputStream()) {
                    log.info("Upload file Success : " );
                    Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }else{
                try (InputStream in = file.getInputStream()) {
                    log.info("Upload file Success : " );
                    Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("Uploaded the file Success : " );
        return ResponseEntity.ok("File uploaded successfully.");
    }

    private String getFileName(MultipartFile file){
        String fileName = file.getOriginalFilename();
        return fileName;
    }

    private void deleteFile(String url_file_path){
       String file_path = url_file_path.replace("/","\\");
        Path path = Paths.get(file_path);
        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/getAllData/{id}")
    public ResponseEntity<?> getAllDataFromCsv(@PathVariable("id") Long id) {
        try {
            Optional<Website> otp = webRepo.getWebsiteById(id);
            if(otp.isEmpty()){
                ResponseError error = new ResponseError();
                error.setErrorMessage("Không tìm thấy id này");
                log.error("ID not found: " );
                return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
            }else{
                Website website = otp.get();
                String fileName = website.getSpider_url().substring(website.getSpider_url().lastIndexOf("/") + 1);
                if(fileName.endsWith(".py")){
                    String script_load_data_file_path = root + "\\" + fileName;

                    String pythonScriptPath = "E:\\python_leaning\\TestPython\\crawler\\crawler\\spiders\\testMethod.py";
                    ProcessBuilder pb = new ProcessBuilder("python", pythonScriptPath, script_load_data_file_path);
                    pb.redirectErrorStream(true);
                    Process process = pb.start();pb.start();
                    BufferedReader bfr = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = "";
                    while ((line = bfr.readLine()) != null) {
                        System.out.println(line);
                    }

                    Thread.sleep(1000);
                    ObjectMapper objectMapper = new ObjectMapper();

                    List<WebsiteDescription> websites = objectMapper.readValue(new File(json_rs_file_path),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, WebsiteDescription.class));
                    for (WebsiteDescription websiteDescription: websites){
                        if(!(websiteDescription.getLink().startsWith("http"))){
                            websiteDescription.setLink("https://" + website.getWebsite_url() + websiteDescription.getLink());
                        }
                    }
                    return new ResponseEntity<>(websites,HttpStatus.OK);
                }
                ResponseError error = new ResponseError("File type is not python file");
                log.error("File type is not python file");
                return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        log.error("Run python script code in cmd Fail : " );
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllData() throws IOException {
        List<Website> li = webRepo.findAll();
        List<WebsiteData> dataList = new ArrayList<>();
        for (Website website : li){
            WebsiteData data = new WebsiteData();
            data.setWebsiteName(website.getWebsite_name());
            data.setWebsiteId(website.getId());

            String fileName = website.getSpider_url().substring(website.getSpider_url().lastIndexOf("/") + 1);
            if(fileName.endsWith(".py")){
                String script_load_data_file_path = root + "\\" + fileName;

                String pythonScriptPath = "E:\\python_leaning\\TestPython\\crawler\\crawler\\spiders\\testMethod.py";
                ProcessBuilder pb = new ProcessBuilder("python", pythonScriptPath, script_load_data_file_path);
                pb.redirectErrorStream(true);
                Process process = pb.start();pb.start();
                BufferedReader bfr = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = "";
                while ((line = bfr.readLine()) != null) {
                    System.out.println(line);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                List<WebsiteDescription> websites = objectMapper.readValue(new File(json_rs_file_path),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, WebsiteDescription.class));
                for (WebsiteDescription websiteDescription: websites){
                    if(!(websiteDescription.getLink().startsWith("http"))){
                        websiteDescription.setLink("https://" + website.getWebsite_url() + websiteDescription.getLink());
                    }
                }
                data.setWebsiteDescription(websites);
                dataList.add(data);
            }else{
                dataList.add(data);
            }
        }
        if (dataList.size() > 0) {
            log.info("Get all data successfully");
            return new ResponseEntity<>(dataList,HttpStatus.OK);
        }
        ResponseError error = new ResponseError("Data empty");
        log.error("Get all data return empty value");
        return new ResponseEntity<>(error,HttpStatus.OK);
    }



}