package com.kh.demo.tools;

import com.kh.demo.DemoApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/*
*   DemoDataGenerator : 데모 데이터 백필을 수동으로 실행하기 위한 진입점.
*
*   IDE에서 이 main()만 직접 실행한다(웹서버는 안 띄움). CommandLineRunner나 테스트로 만들지 않은 이유는
*   평소 앱 부팅/빌드 때 실수로 같이 돌면 안 되는, 명시적으로 사람이 불러야 하는 1회성 배치이기 때문.
* */
public class DemoDataGenerator {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(DemoApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
        try {
            context.getBean(DemoDataService.class).generate();
        } finally {
            context.close();
        }
    }
}
