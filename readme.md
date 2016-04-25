[![Build Status](https://travis-ci.org/RutledgePaulV/monads.svg?branch=develop)](https://travis-ci.org/RutledgePaulV/monads)
[![Coverage Status](https://coveralls.io/repos/github/RutledgePaulV/monads/badge.svg?branch=develop)](https://coveralls.io/github/RutledgePaulV/monads?branch=develop)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.rutledgepaulv/monads/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.rutledgepaulv/monads)

## Monads
Basic useful monads missing from Java.


#### Lazy
```java
// maintain the lazy as a memoizing supplier
// every time you call .get() you get the same instance
private Lazy<UserService> userService = Lazy.of(UserServiceImpl::new);

// create a proxy for the interface that delegates to a lazy instance.
// this means that hte instance only gets created once you call a method on the proxy.
private UserService userService = Lazy.proxy(UserService.class, UserServiceImpl::new);
```

#### Try
```java
public String getUserDisplay(String username) {
    return queryDatabase(username)
            .map(User::getDisplayName)
            .recover(NotFoundException.class, ex -> "User not found.")
            .recover(IOException.class, ex -> "Error communicating with database.")
            .onFailure(Exception.class, ex -> logger.error("Encountered unhandled error.", ex))
            .get();
}

// by returning a Try from your methods that can fail with exceptions you
// provide a control mechanism to the higher layers in your application 
// which are the ones that actually know enough to decide how to respond 
// to failures
public Try<User> queryDatabase(String username) {
    return Try.of(() -> findOne(where("username").is(username)));
}
```


#### Release Versions
```xml
<dependencies>
    <dependency>
        <groupId>com.github.rutledgepaulv</groupId>
        <artifactId>monads</artifactId>
        <version>0.5</version>
    </dependency>
</dependencies>
```

#### Snapshot Versions
```xml
<dependencies>
    <dependency>
        <groupId>com.github.rutledgepaulv</groupId>
        <artifactId>monads</artifactId>
        <version>0.6-SNAPSHOT</version>
    </dependency>
</dependencies>

<repositories>
    <repository>
        <id>ossrh</id>
        <name>Repository for snapshots</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

### License

This project is licensed under [MIT license](http://opensource.org/licenses/MIT).