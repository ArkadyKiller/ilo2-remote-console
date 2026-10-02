# iLO2 Remote Console — standalone (Windows .exe)

Standalone launcher for the **HP iLO2 Remote Console (KVM)**. The original iLO2
Java applet is converted into a standalone Windows application with a simple
login form — **no browser and no Java Web Start needed**.

## Для чего / Purpose
- Подключение к KVM-консоли сервера HP iLO2 без браузера и Java-апплета.
- Простая форма входа: **адрес сервера и порт**, **логин**, **пароль**.
- Прямой запуск как обычного Windows-приложения (`.exe`).

## Требования / Requirements
- **Java устанавливать НЕ обязательно.** В готовом `.exe` **встроен JRE 8** (нужен
  для устаревшего TLS iLO2). Ничего дополнительно ставить не надо.
- Windows x64.
- Для **сборки из исходников** требуется **JDK 8**.

## Порты / Ports
| Порт | Назначение | По умолчанию |
|---|---|---|
| **Web / SSL (HTTPS)** | вход/логин iLO (поле «адрес:порт») | **443** (пример теста: 444) |
| **Console (telnet)** | KVM-консоль; сервер сообщает порт в ответе (`INFO6`) | **23** |
| **Media** | совместимость с iLO4 | 17988 |
| **Console replay** | совместимость с iLO4 | 17990 |

- Поле адреса принимает `host` или `host:port` (например `192.168.0.24:444`).
  Если порт не указан — используется **443**.
- Для консоли iLO2 **нужен порт 23** (сервер отдаёт его как `INFO6`). Откройте/пробросьте его.
- Порты 17988/17990 — для iLO4-подобных консолей (в форме это advanced-поля).

## Использование / Usage
1. Запустите **`iLO2RemoteConsole.exe`**.
2. Заполните: **Адрес** (`host` или `host:port`), **Логин**, **Пароль**.
3. Нажмите **«Подключиться»** — откроется окно KVM-консоли.
4. При закрытии окна выполняется **logout** (сессия на iLO освобождается).

## Сборка / Build (из исходников)
Требуется JDK 8:
```bat
javac -encoding UTF-8 -d build\classes src\main\java\*.java src\main\java\com\hp\ilo2\remcons\*.java
jar cfe dist\ILO2RemCon.jar Main -C build\classes .
```
Упаковка в `.exe` — через [WinRun4J](https://github.com/poidasmith/winrun4j)
(конфиг `iLO2RemoteConsole.ini`):
```
main.class=Main
classpath.1=ILO2RemCon.jar
vmarg.1=-Djava.security.properties=java.security
working.directory=.
vm.location=jre\bin\server\jvm.dll
```
(папка `jre\` — встроенный JRE 8).

## Важно / Security
- iLO2 использует **устаревший TLS** (TLSv1.0 / RC4). Приложение ослабляет
  `java.security` и **отключает проверку сертификата** (`SSLUtilities.trustAll*`).
  Используйте только в доверенной сети.
- Не храните пароли в репозитории (используйте форму или `config.properties`, не коммитьте его).

## Происхождение / Credits
- Основано на `scrapes/ILO2-Standalone-Remote-Console` (содержит код консоли HP iLO2).
- HP iLO2 Remote Console © Hewlett-Packard. Код предоставляется «как есть» (as-is)
  для пользователей iLO2. Все права на оригинальный код принадлежат HP.
