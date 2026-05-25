# CenterMessage

[![Minecraft](https://img.shields.io/badge/Minecraft-1.16%2B-dark_green.svg)](https://shields.io/)
[![Java](https://img.shields.io/badge/Java-8-orange.svg)](https://shields.io/)
[![JitPack](https://jitpack.io/v/senkex/CenterMessage.svg)](https://jitpack.io/#senkex/CenterMessage)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

Adventure-powered library to center chat messages in Minecraft. Mix legacy
`&` / `§`, hex `&#RRGGBB`, Spigot hex `§x§R§R§G§G§B§B` and MiniMessage tags
(`<color>`, `<gradient>`, `<bold>`, ...) in the same string and it parses
through a single pipeline. Color codes never push the text off-center —
only visible glyphs contribute to the leading padding.

> [!IMPORTANT]
> Requires Minecraft **1.16 or newer** and Adventure at runtime. Paper ships
> Adventure embedded; on Spigot you must shade `adventure-platform-bukkit`
> yourself.

> [!CAUTION]
> Don't forget to [shade](#shading) the library if you ship it inside a
> plugin, otherwise it will clash with any other plugin bundling its own copy.

The library is a single static facade. No plugin instance, no `onEnable` hook,
no config files — import it and call the static methods.

### Getting Started

Compiled for **Java 8** bytecode so it fits whatever JDK Paper or Spigot is
using on your version. Bukkit / Paper-API, PlaceholderAPI and Adventure are
all `compileOnly` dependencies of the library.

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

Send a centered message to a `CommandSender`. The library uses Adventure
when the sender supports it (Paper), falls back to legacy `sendMessage`
otherwise:

```java
CenterMessage.send(player, "&aHello &b<bold>World</bold>");
```

Get the centered string back:

```java
String line = CenterMessage.center("&#FF55AA Centered &ltext");
player.sendMessage(line);
```

Or work directly with Adventure `Component`s:

```java
Component comp = CenterMessage.centerComponent("<gradient:#f00:#0f0>Hi</gradient>");
player.sendMessage(comp);

// Re-center any existing component
Component custom = Component.text("hello").color(NamedTextColor.AQUA);
player.sendMessage(CenterMessage.centerComponent(custom));
```

Multi-line and `<center>` blocks:

```java
CenterMessage.sendBlock(player,
        "<center>&6&lWelcome</center>\n" +
        "&7Use /help to see commands.\n" +
        "<center>&7Have fun.</center>");
```

### Options

Default is chat width. For signs, books, anvils or custom widths use
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

### PlaceholderAPI

If PlaceholderAPI is installed on the server, every method that receives a
`Player` or `OfflinePlayer` expands placeholders against that target before
the message is parsed. No hard dependency is needed — the bridge is a
reflection probe done once when the library loads.

```java
CenterMessage.send(player, "&6Balance: &a%vault_eco_balance%");
```

### Drop-in colorize

`CenterMessage.colorize` is a drop-in replacement for
`ChatColor.translateAlternateColorCodes` that also understands `&#RRGGBB`
and MiniMessage tags:

```java
String colored = CenterMessage.colorize("&aHello &#FF55AA world <bold>!</bold>");
```

## Shading

If you're shipping CenterMessage inside a plugin you **must** relocate it.
Two plugins on the same server depending on different versions of the same
package will eventually break someone's day.

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

- Pipeline order: PlaceholderAPI → `&#RRGGBB` → `&x` → legacy-to-MiniMessage
  conversion → MiniMessage parse. Mixing formats in the same string is fine.
- Width is measured against the vanilla Minecraft font. Custom resource packs
  can override widths through `FontInfo.registerWidth(char, int)`.
- Bold formatting is honored end-to-end: nested `<bold>` blocks and legacy
  `§l` both apply the correct extra pixel per character when measuring.

### License

Released under the MIT License. Do whatever you want with it, attribution is appreciated.
