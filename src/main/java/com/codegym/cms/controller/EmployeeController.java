package com.codegym.cms.controller;

import com.codegym.cms.model.Department;
import com.codegym.cms.model.Employee;
import com.codegym.cms.model.EmployeeForm;
import com.codegym.cms.service.DepartmentService;
import com.codegym.cms.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

@Controller
@PropertySource("classpath:global_config_app.properties")
public class EmployeeController {
    @Autowired
    private Environment env;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private EmployeeService employeeService;
    @ModelAttribute("department")
    public Iterable<Department> departments(){
        return departmentService.findAll();
    }
    //create-employee
    @GetMapping("/create-employee")
    public ModelAndView showCreateForm(){
        ModelAndView modelAndView = new ModelAndView("/employee/create");
        modelAndView.addObject("employeeForm",new EmployeeForm());
        return modelAndView;
    }
//    @PostMapping("/create-employee")
//    public ModelAndView saveEmployee(@ModelAttribute("employee")Employee employee){
//        employeeService.save(employee);
//        ModelAndView modelAndView = new ModelAndView("/employee/create");
//        modelAndView.addObject("employee",new Employee());
//        modelAndView.addObject("message","New Employee created successfully");
//        return modelAndView;
//    }
@PostMapping("/create-employee")
public ModelAndView saveEmployee(@Validated @ModelAttribute("employeeForm") EmployeeForm employeeForm, BindingResult result) {

    // thong bao neu xay ra loi
    if (result.hasErrors()) {
        ModelAndView modelAndView = new ModelAndView("/employee/create");
        modelAndView.addObject("employeeForm", new EmployeeForm());
        return modelAndView;
    }

    // lay ten file
    MultipartFile multipartFile = employeeForm.getAvatar();
    String fileName = multipartFile.getOriginalFilename();
    String fileUpload = env.getProperty("file_upload").toString();

    // luu file len server
    try {
        //multipartFile.transferTo(imageFile);
        FileCopyUtils.copy(employeeForm.getAvatar().getBytes(), new File(fileUpload + fileName));
    } catch (IOException ex) {
        ex.printStackTrace();
    }
    // tham khảo: https://github.com/codegym-vn/spring-static-resources

    // tao doi tuong de luu vao db
   Employee employee = new Employee(employeeForm.getName(), employeeForm.getBirthDate(),
           employeeForm.getAddress(),fileName,employeeForm.getSalary(),employeeForm.getDepartment());

    // luu vao db
    //productService.save(productObject);
    employeeService.save(employee);

    ModelAndView modelAndView = new ModelAndView("/employee/create");
    modelAndView.addObject("employee", new EmployeeForm());
    modelAndView.addObject("message","successes!");
    return modelAndView;

}
    @GetMapping("/employees")
    public ModelAndView listEmployees(@RequestParam("s")Optional<String> s, @PageableDefault(value = 10, sort = "salary")Pageable pageable){
        Page<Employee> employees;
        if(s.isPresent()){
          employees = employeeService.findAllByName(s.get(),pageable);

        }else {
            employees = employeeService.findAll(pageable);
        }
        ModelAndView modelAndView = new ModelAndView("/employee/list");
        modelAndView.addObject("employee",employees);
        return modelAndView;
    }
    @GetMapping("/edit-employee/{id}")
    public ModelAndView showEditForm(@PathVariable Long id){
        Employee employee = employeeService.findById(id);
        if(employee != null){
            ModelAndView modelAndView = new ModelAndView("/employee/edit");
            modelAndView.addObject("employee",employee);
            return modelAndView;
        }else {
            ModelAndView modelAndView = new ModelAndView("/error.404");
            return modelAndView;
        }
    }
    @PostMapping("/edit-employee")
    public ModelAndView updateEmployee(@ModelAttribute("employee")EmployeeForm employeeForm){
       Long id = employeeForm.getId();
        String name =employeeForm.getName();
        LocalDate birthDate = employeeForm.getBirthDate();
        String  address = employeeForm.getAddress();
        String fileName = employeeForm.getAvatar().getOriginalFilename();
        Double salary = employeeForm.getSalary();
        Department department = employeeForm.getDepartment();

        Employee employee = new Employee(id,name,birthDate,address,fileName,salary,department);

        employeeService.save(employee);

        ModelAndView modelAndView = new ModelAndView("/employee/edit");
        modelAndView.addObject("employees", employee);
        modelAndView.addObject("message","Employee updated successfully");
                return modelAndView;
    }
    @GetMapping("/delete-employee")
    public ModelAndView showDeleteForm(@PathVariable Long id){
        Employee employee = employeeService.findById(id);
        if(employee !=null){
            ModelAndView modelAndView = new ModelAndView("/employee/delete");
            modelAndView.addObject("employee",employee);
            return modelAndView;
        }else {
            ModelAndView modelAndView = new ModelAndView("/error.404");
            return modelAndView;
        }
    }
    @PostMapping("/delete-employee")
    public String deleteCustomer(@ModelAttribute("employee")Employee employee){
        employeeService.remove(employee.getId());
        return "redirect:employees";
    }

}
