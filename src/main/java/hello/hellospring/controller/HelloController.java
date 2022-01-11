package hello.hellospring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

    @GetMapping("hello") // GET ~~~/hello 접근시
    public String hello(Model model){
        model.addAttribute( "data", "hello!!");
        return "hello"; // resources/templetes+{hello}.html로 연결
    }

    @GetMapping("hello-mvc") // GET ~~~/hello-mvc 접근시
    public String helloMVC(@RequestParam("name") String name, Model model){
        model.addAttribute("name",name);
        return "hello-temp";
    }

    @GetMapping("hello-string")
    @ResponseBody // data를 그대로 준다
    public String helloString(@RequestParam("name") String name){
        return "hello"+name;
    }

    @GetMapping("hello-api")
    @ResponseBody // json 형태로 반환 {"name" : value}
    public Hello helloApi(@RequestParam("name") String name){
        Hello ob = new Hello();
        ob.setName(name);
        return ob; // 객체 return : json 형태
    }

    static class Hello{
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
