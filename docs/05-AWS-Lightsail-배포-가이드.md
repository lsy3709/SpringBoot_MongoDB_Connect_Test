# AWS Lightsail 배포 가이드 (Git 클론 후 서버 시작)

AWS Lightsail 인스턴스에서 Git으로 프로젝트를 클론하고, MongoDB(Docker)와 함께 Spring Boot 서버를 실행하는 방법입니다.

---

## 사전 요구사항

| 항목   | 버전    | 비고                     |
| ------ | ------- | ------------------------ |
| Java   | 17 이상 | `java -version`으로 확인 |
| Git    | 최신    | `git --version`으로 확인 |
| Docker | 최신    | MongoDB 컨테이너용       |

### Java 17 설치 (없는 경우)

```bash
# Amazon Linux 2
sudo amazon-linux-extras install java-openjdk17 -y

# Ubuntu
sudo apt update
sudo apt install openjdk-17-jdk -y

# 설치 확인
java -version
```

### Git 설치 (없는 경우)

```bash
# Amazon Linux 2
sudo yum install git -y

# Ubuntu
sudo apt install git -y

# 설치 확인
git --version
```

### Docker 설치 (없는 경우)

```bash
# Amazon Linux 2
sudo yum install docker -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER  # 재로그인 후 적용

# Ubuntu
sudo apt update
sudo apt install docker.io -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER  # 재로그인 후 적용

# 설치 확인
docker --version
```

---

## 1. 저장소 클론

```bash
# 작업 디렉토리로 이동
cd ~  # 또는 원하는 경로

# 저장소 클론 (예: GitHub)
git clone https://github.com/lsy3709/SpringBoot_MongoDB_Connect_Test.git

# 프로젝트 폴더로 이동
cd SpringBoot_MongoDB_Connect_Test
```

> **참고**: 저장소가 Private이면 토큰 또는 SSH 키 설정이 필요합니다.

---

## 2. MongoDB 실행 (Docker)

같은 머신에서 MongoDB를 Docker로 실행합니다.

```bash
# MongoDB 6.x 컨테이너 실행 (데이터 영속을 위해 볼륨 마운트)
docker run -d \
  --name mongodb \
  -p 27017:27017 \
  -v mongodb_data:/data/db \
  mongo:6.0

# 실행 확인
docker ps
```

- **포트**: `27017` (application.yml 기본값과 동일)
- **볼륨**: 컨테이너 재시작 후에도 데이터 유지

### MongoDB 중지/재시작

```bash
# 중지
docker stop mongodb

# 재시작
docker start mongodb
```

---

## 3. application.yml 확인

기본 MongoDB URI는 `mongodb://localhost:27017/blog3` 입니다.  
같은 머신에서 MongoDB를 27017 포트로 실행 중이면 별도 수정이 필요 없습니다.

```yaml
# src/main/resources/application.yml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/blog3
```

---

## 4. 프로젝트 빌드 및 실행

### 4-1. Gradle로 직접 실행

```bash
# 프로젝트 루트에서
./gradlew bootRun

# 또는 배포용 프로파일 사용 시 (운영 환경)
./gradlew bootRun --args='--spring.profiles.active=prod'
```

> Windows에서 `gradlew`가 없다면 `gradlew.bat bootRun` 사용.  
> Linux에서는 `chmod +x gradlew` 후 `./gradlew bootRun`

### 4-2. JAR 빌드 후 실행

```bash
# JAR 빌드 (테스트 제외)
./gradlew build -x test

# JAR 실행 (build/libs/ 아래에 생성됨)
java -jar build/libs/SpringBoot_MongoDB_Connect_Test-0.0.1-SNAPSHOT.jar

# 운영 프로파일 적용 시
java -jar build/libs/SpringBoot_MongoDB_Connect_Test-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

---

## 5. 백그라운드 실행 (nohup)

터미널 종료 후에도 서버가 계속 동작하도록 합니다.

```bash
# JAR 빌드 후
nohup java -jar build/libs/SpringBoot_MongoDB_Connect_Test-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# 로그 확인
tail -f app.log
```

---

## 6. systemd 서비스로 등록 (권장)

재부팅 시 자동 기동을 위해 systemd 서비스로 등록합니다.

```bash
# 서비스 파일 생성
sudo nano /etc/systemd/system/spring-mongodb.service
```

내용:

```ini
[Unit]
Description=Spring Boot MongoDB App
After=network.target docker.service
Requires=docker.service

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/SpringBoot_MongoDB_Connect_Test
ExecStart=/usr/bin/java -jar /home/ubuntu/SpringBoot_MongoDB_Connect_Test/build/libs/SpringBoot_MongoDB_Connect_Test-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10
Environment="SPRING_PROFILES_ACTIVE=prod"

[Install]
WantedBy=multi-user.target
```

> `User`, `WorkingDirectory`, `ExecStart` 경로를 실제 환경에 맞게 수정하세요.

```bash
# 서비스 활성화 및 시작
sudo systemctl daemon-reload
sudo systemctl enable spring-mongodb
sudo systemctl start spring-mongodb
sudo systemctl status spring-mongodb

# 로그 확인
실시간 로그 모니터링 (추천)
서버를 띄워놓고 로그가 올라오는 것을 실시간으로 보고 싶을 때 사용합니다.

Bash
sudo journalctl -u spring-mongodb.service -f
```

최근 로그 일부만 보기
Bash
sudo journalctl -u spring-mongodb.service -n 100 --no-pager

---

3. 배포 후 업데이트 과정 (CI/CD의 기초)
   코드를 수정하고 다시 서버에 반영할 때는 아래의 5단계 루틴을 따르시면 됩니다.

STEP 1: 최신 코드 가져오기 (Git 사용 시)
Bash
git pull origin main
STEP 2: 기존 빌드 파일 삭제 및 재빌드
이전에 빌드된 잔여물을 지우고 새로 빌드합니다.

Bash
sudo ./gradlew clean build -x test
STEP 3: 서비스 중지 (선택 사항)
새 JAR 파일을 덮어쓰기 전에 안전하게 서비스를 멈춥니다. (사실 Restart만 해도 되지만, 안정성을 위해 권장합니다.)

Bash
sudo systemctl stop spring-mongodb
STEP 4: 서비스 재시작
새로 빌드된 JAR 파일을 시스템이 다시 읽도록 합니다.

Bash
sudo systemctl daemon-reload # (서비스 파일 내용도 바꿨을 때만 필수)
sudo systemctl restart spring-mongodb
STEP 5: 정상 가동 확인
Bash
sudo systemctl status spring-mongodb

# 이후 실시간 로그(journalctl -f)로 앱이 완전히 떴는지 확인

4. 꿀팁: 업데이트 스크립트 만들기
   매번 위 명령어를 치기 귀찮다면, deploy.sh 파일을 만들어 한 번에 실행할 수 있습니다.

Bash

# 1. 파일 생성

vi deploy.sh
내용 작성:

Bash
#!/bin/bash
echo ">>> 빌드 시작"
sudo ./gradlew clean build -x test

echo ">>> 서비스 재시작"
sudo systemctl daemon-reload
sudo systemctl restart spring-mongodb

echo ">>> 배포 완료! 3초 후 로그를 확인합니다."
sleep 3
sudo journalctl -u spring-mongodb -n 20
실행 권한 부여 및 사용:

Bash
chmod +x deploy.sh
./deploy.sh # 이제 이것만 실행하면 업데이트 끝!

---

## 8. 요약 체크리스트

| 순서 | 작업                | 명령어                                                                           |
| ---- | ------------------- | -------------------------------------------------------------------------------- |
| 1    | 저장소 클론         | `git clone <저장소URL>`                                                          |
| 2    | MongoDB 실행        | `docker run -d --name mongodb -p 27017:27017 -v mongodb_data:/data/db mongo:6.0` |
| 3    | 빌드                | `./gradlew build -x test`                                                        |
| 4    | 실행                | `java -jar build/libs/SpringBoot_MongoDB_Connect_Test-0.0.1-SNAPSHOT.jar`        |
| 5    | (선택) systemd 등록 | 위 6번 참고                                                                      |

---

## 참고

- **포트**: 기본 포트는 `8080`입니다. 변경하려면 `server.port` 설정 추가.
- **방화벽**: Lightsail 인스턴스의 방화벽(네트워크)에서 8080, 22 포트 허용이 필요합니다.
- **HTTPS**: SSL/도메인 설정은 Nginx 리버스 프록시 및 Let's Encrypt 등을 검토하세요.
