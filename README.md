# CenterMessage

[![Minecraft](https://img.shields.io/badge/Minecraft-1.7%2B-dark_green.svg)](https://shields.io/)
[![Java](https://img.shields.io/badge/Java-8-orange.svg)](https://shields.io/)
[![JitPack](https://jitpack.io/v/senkex/CenterMessage.svg)](https://jitpack.io/#senkex/CenterMessage)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

Small library to center messages in Minecraft chat. It measures the rendered pixel width of the
visible glyphs only, so color codes never push the text off-center. Legacy `&`/`§`, BungeeCord
hex `&#RRGGBB`, Spigot hex `§x§R§R§G§G§B§B` and MiniMessage tags (`<color>`, `<gradient>`,
`<bold>`, ...) are all stripped from the calculation.

> [!IMPORTANT]
> Compatible with Minecraft **1.7 and newer**. Hex colors only render on 1.16+ clients, but the
> spacing is computed correctly on every version.

> [!CAUTION]
> Don't forget to [shade](#shading) the library if you ship it inside a plugin, otherwise it will
> clash with any other plugin bundling its own copy.

The library is a single static facade. No plugin instance, no `onEnable` hook, no config files —
import it and call the static methods.

### Getting Started

Targets **Java 8** to keep the surface compatible with legacy servers. Bukkit/Spigot, PlaceholderAPI
and Adventure are all `compileOnly` and detected at runtime through reflection, so adding the
dependency never forces them onto your plugin.

You can drop it into your project with JitPack:

#### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

```xml
<dependency>
    <groupId>com.github.senkex</groupId>
    <artifactId>CenterMessage</artifactId>
    <version>version</version>
</dependency>
```

#### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.senkex:CenterMessage:version")
}
```

#### Gradle (Groovy)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.senkex:CenterMessage:version'
}
```

### Usage

The simplest case is sending a centered message to a `CommandSender`:

```java
CenterMessage.send(player, "&aHello &b<bold>World</bold>");
```

If you just want the centered string back, use `center`:

```java
String line = CenterMessage.center("&#FF55AA Centered &ltext");
player.sendMessage(line);
```

Multi-line messages can be centered line by line, or you can wrap only the lines you want with
`<center>` tags:

```java
CenterMessage.sendBlock(player,
        "<center>&6&lWelcome</center>\n" +
        "&7Use /help to see commands.\n" +
        "<center>&7Have fun.</center>");
```

Anything outside a `<center>` block is sent verbatim, so you can mix centered headers with
left-aligned body text without splitting the message yourself.

### Options

The static methods default to chat width. For signs, books, anvils or any custom width use a
`CenterOptions`:

```java
CenterMessage.center("&aTitle", CenterOptions.SIGN);
CenterMessage.center("&aTitle", CenterOptions.BOOK);

CenterOptions custom = CenterOptions.builder()
        .centerPx(80)
        .parseMiniMessage(false)
        .build();

CenterMessage.send(player, "&aTitle", custom);
```

Every parser flag can be disabled independently if you want to skip work on messages that you know
don't use a given format.

### PlaceholderAPI

If PlaceholderAPI is installed on the server, every method that receives a `Player` or
`OfflinePlayer` expands placeholders against that target before measuring the width:

```java
CenterMessage.send(player, "&6Balance: &a%vault_eco_balance%");
```

No dependency is required; the bridge is a reflection probe done once when the library loads.

### MiniMessage

Tags such as `<color>`, `<#FF55AA>`, `<gradient:...>`, `<bold>`, `<reset>` and friends are stripped
from the width calculation regardless of whether Adventure is on the classpath. When Adventure is
available, the tags are also converted to legacy `§` before sending, so the colors render on
servers that don't ship Adventure natively.

### Drop-in colorize

`CenterMessage.colorize` is a drop-in replacement for
`ChatColor.translateAlternateColorCodes` that also understands `&#RRGGBB` and, if Adventure is
present, MiniMessage:

```java
String colored = CenterMessage.colorize("&aHello &#FF55AA world &lbold");
```

## Shading

If you're shipping CenterMessage inside a plugin you **must** relocate it. Two plugins on the same
server depending on different versions of the same package will eventually break someone's day.

### Maven Shade Plugin

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.6.0</version>
    <configuration>
        <relocations>
            <relocation>
                <pattern>com.github.senkex.centermessage</pattern>
                <!-- change this to a package inside your own plugin -->
                <shadedPattern>my.plugin.libs.centermessage</shadedPattern>
            </relocation>
        </relocations>
    </configuration>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Gradle Shadow (Kotlin DSL)

```kotlin
plugins {
    id("com.gradleup.shadow") version "8.3.5"
}

tasks {
    shadowJar {
        relocate("com.github.senkex.centermessage", "my.plugin.libs.centermessage")
    }
}
```

### Gradle Shadow (Groovy)

```groovy
plugins {
    id 'com.gradleup.shadow' version '8.3.5'
}

tasks {
    shadowJar {
        relocate 'com.github.senkex.centermessage', 'my.plugin.libs.centermessage'
    }
}
```

### Notes

- Width is measured against the vanilla Minecraft font. Resource packs with custom widths can
  override individual characters through `FontInfo.registerWidth(char, int)`.
- The cache for the optional integrations is built once at class load. There's nothing to
  configure and nothing to shut down.
- Bold formatting is tracked across both legacy codes and MiniMessage tags, including nested
  `<bold>` blocks.

### License

Released under the MIT License. Do whatever you want with it, attribution is appreciated.
