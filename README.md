# Spring-boot
## 목차
* [스프링 프로젝트 환경설정](#스프링-프로젝트-환경설정)
* [스프링 웹 기초](#스프링-웹-기초)
  * [Welcome page](#1-welcome-page)
  * [MVC](#2-mvc)
* [백엔드 개발 예제](#백엔드-개발-예제)
  * [간단한 소개](#1-간단한-소개)
  * [도메인](#2-domain)
  * [리포지토리](#3-repository)
  * [서비스](#4-service)
  * [컨트롤러](#5-controller)
  * [테스트 케이스](#6-controller)
  * [스프링 빈](#7-spring-bean)
* 스프링 DB 기술
  * jdbcTemplate
  * JPA
--------
## 스프링 프로젝트 환경설정
- 스프링 부트 스타터 사이트 (https://start.spring.io)
- 다음과 같이 설정 후 프로젝트를 생성한다.

<img alt="" src="https://user-images.githubusercontent.com/86395683/149112784-9bb4257b-42b6-4bde-9579-4c34a0409ac8.PNG">

- `````"Project name" Application.java````` file -> main 함수가 있어 실행 가능하다.
- 실행 시 내장 되어 있는 tomcat 웹 서버가 실행되는 것을 확인할 수 있다.
- ```build.gradle``` : 이곳에서 각종 라이브러리를 추가한다.
---------
## 스프링 웹 기초
### 1. Welcome page
- spring에서는 default welcome page로 ```src/main/resources/static/index.html```에서 첫 페이지를 불러온다.
### 2. MVC

- Model, View, Controller로 이루어진 디자인패턴이다.
 

- Model 객체는 ```addAttribute``` 매서드를 이용해 ```atturbuteName```과 ```attributeValue```를 추가 할 수 있고 \
이를 ```view(html)```에서 ```${attributeName}```을 통해 value 값을 호출할 수 있다.


- Controller는 client의 여러 변경 요청을 받아 Model과 View를 변경하여야 한다.
- ```resources/templates``` 에 있는 html을 ```return``` 받는다.
```java
@Controller
public class HelloController {

    @GetMapping("hello") // GET ./hello 접근시
    public String hello(Model model){
        model.addAttribute( "data", "hello!!");
        return "hello"; // hello.html 호출
    }
    
    @GetMapping("hello-mvc") // GET ./hello-mvc 접근시
    public String helloMVC(@RequestParam("name") String name, Model model){
        model.addAttribute("name",name);
        return "hello-temp";
  }
}
```
- ```@RequestParam``` : ```hello-temp?name=abcd``` 식으로 호출받아 ```attirbutename:"name"```의 ```value = "abcd"```로 설정
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>HELLO</title>
    <meta http-equiv="content-type" content="text/html;" charset="utf-8">
</head>
<body>
<p th:text="'안녕하세요.'+ ${data}" ></p>
</body>
</html>
```
-----
## 백엔드 개발 예제
### 1. 간단한 소개
- DB : Id(자동으로 부여), name(사용자가 등록)
- 기능 : 회원 등록, 조회


- domain: Member class 구현
- repository: DB 저장소에 따라 class 분리해 다르게 구현
  - 따라서 공통으로 사용 가능하도록 ```interface``` 구현
- service: 회원 등록 및 회원 조회 기능 구현
- controller: MVC 패턴을 관리하는 controller

### 2. Domain
- Class Member -> id, name
```java
public class Member {
  private Long id; 
  private String name;
}
```
### 3. Repository
(1) interface MemberRepository
- 공통이 되는 기능들을 포함한 interface
```java
public interface MemberRepository {
    Member save(Member member); 
    Optional<Member> findById(Long id); 
    Optional<Member> findByName(String name);
    List<Member> findAll();

}
```
(2) class MemoryMemberRepository
- 참고사항
  - [stream과 lambda 표현식](https://huisam.tistory.com/entry/stream)
  - [HashMap Method](https://docs.oracle.com/javase/8/docs/api/java/util/HashMap.html)
```java
public class MemoryMemberRepository implements MemberRepository{

    private static Map<Long,Member> store = new HashMap<>(); // <key,value>
    private static long sequence = 0L;

    @Override
    public Member save(Member member) {
        member.setId(++sequence);
        store.put(member.getId(), member);
        //put(key,value)
        return member;
    }

    @Override
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(store.get(id));
        // get(key) -> return value
    }

    @Override
    public Optional<Member> findByName(String name) { 
        return store.values().stream()
                .filter(member -> member.getName().equals(name))
                .findAny();
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    public void clearStore(){
        store.clear();
    }
}
```
### 4. Service
```java
public class MemberService {
    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 회원 가입
    public Long join(Member member){
        //중복 금지
        ValidDuplicateMember(member);

        memberRepository.save(member);
        return member.getId();
    }

    private void ValidDuplicateMember(Member member) {
        // ifPresent : NULL이 아닐 시 내부 함수로
        Optional<Member> result = memberRepository.findByName(member.getName());
        result.ifPresent(m -> {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        });
    }

    // 전체 회원 조회
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }
    // 한 회원 조회
    public Optional<Member> findOne(Long memberId){
        return memberRepository.findById(memberId);
    }
}
```
### 5. Controller
#### home.html
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<body>
<div class="container">
    <div>
        <h1>Hello Spring</h1>
        <p>회원 기능</p>
        <p>
            <a href="/members/new">회원 가입</a>
            <a href="/members">회원 목록</a>
        </p>
    </div>
</div> <!-- /container -->
</body>
</html>
```
- ```@GetMapping```을 통해 ```createMemberForm.html```으로 접근 그 후 ```post```방식으로 정보를 전달 받으면 이를 새 Member 객체에 넣어 ```join Service```를 호출한다.

```java
@Controller
public class MemberController {
    private final MemberService memberService;

    @Autowired // spring container에서 가져온다.
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members/new")
    public String createForm(){
        return "members/createMemberForm";
    }

    @PostMapping("/members/new")
    public String create(MemberForm form){
        Member member = new Member();
        member.setName(form.getName());

        memberService.join(member);

        return "redirect:/";
    }

    @GetMapping("/members")
    public String list(Model model){
        List<Member> members = memberService.findMembers();
        model.addAttribute("members",members);
        return "members/memberlist";
    }
}
```
### 6. Test
- given -> when -> then 으로 정리

(1) Repository
- 각 Test가 끝난 후 repository 객체 정리
```java
@AfterEach // Test 호출 후 실행
public void afterEach(){
    repository.clearStore(); // clear !
}
```
- save
  - name = "spring" 인 member 저장 후 repository에서 불러온 member랑 일치하는 지 확인
```java
@Test
public void save() {
    Member member = new Member();
    member.setName("spring");

    repository.save(member);
    Member result = repository.findById(member.getId()).get();

    Assertions.assertEquals(member, result); // member = result 같은지 확인

}
```
- findByName
  - 두 member 저장 후 첫 번째 member 이름으로 찾아 일치하는지 확인
```java
@Test
public void findByName(){
    Member member1 = new Member();
    member1.setName("spring1");
    repository.save(member1);

    Member member2 = new Member(); 
    member2.setName("spring2");
    repository.save(member2);

    Member result = repository.findByName("spring1").get();
    Assertions.assertEquals(result,member1);
}
```
- findAll
  - findByName과 마찬가지로 저장 후 findAll()로 찾아낸 member 수가 2인지 확인
```java
@Test
public void findAll(){
    Member member1 = new Member();
    member1.setName("spring1");
    repository.save(member1);

    Member member2 = new Member(); 
    member2.setName("spring2");
    repository.save(member2);

    List<Member> result = repository.findAll();

    Assertions.assertEquals(result.size(),2);
}
```
(2)Service
- duplicateException
  - join 중에 중복되는 name일 경우 잘 처리하는지 확인
```java
@Test
public void duplicateException(){
    //given
    Member member1 = new Member();
    member1.setName("spring");

    Member member2 = new Member();
    member2.setName("spring");

    //when
    memberService.join(member1);
    IllegalStateException e = assertThrows(IllegalStateException.class, ()->memberService.join(member2));
    Assertions.assertEquals(e.getMessage(),"이미 존재하는 회원입니다.");

    /* try - catch 문
    try{
        memberService.join(member2);
        fail();
    } catch (IllegalStateException e){
        Assertions.assertEquals(e.getMessage(),"이미 존재하는 회원입니다.");
    }
     */
    
}
```
### 7. Spring Bean
(1) Component scan
- ```@Controller, @Service, @Repository```는 ```@Component```를 포함하고 있어 자동으로 스프링 빈에 등록된다.
- ```@Autowired```는 자동으로 Spring Container에서 연관된 객체를 주입해 준다.(Dependency Injection)

(2) 직접 Spring Bean 등록하기
- Config class에 ```@Bean```을 사용하여 구현한다.
```java

@Configuration // Bean 등록하기
public class SpringConfig {
        
    @Bean
    public MemberService memberService(){
        return new MemberService(memberRepository());
    }

    @Bean
    public MemberRepository memberRepository(){
        return new MemoryMemberRepository();
    }


}
```