# ConfigUtil

a config for java(≥16)

here we named ConfigUtil to "CU" for convenient

# Use CU

## Initialization

a simple CU

```java
import com.github.zhuaidadaya.utils.config.ConfigUtil;

public class Test {
    ConfigUtil config;

    public void initConfig() {
        config = new ConfigUtil();
    }
}
```

> CU will create by default settings

<details>
<summary>Reference 
</summary>

```java

public class ConfigUtil {
    public ConfigUtil() {
        defaultUtilConfigs();
        readConfig(true);
    }

    public void defaultUtilConfigs() {
        addUtilConfig("path", System.getProperty("user.dir"));
        addUtilConfig("name", "config.mhf");
        addUtilConfig("version", "1.2");
        addUtilConfig("autoWrite", true);
        addUtilConfig("inseparableLevel", 3);
        addUtilConfig("encryptionHead", false);
        addUtilConfig("encryption", false);
    }
}

```

</details>

<hr>

here is settings to init CU

```java
import com.github.zhuaidadaya.utils.config.ConfigUtil;

public class Test {
    ConfigUtil config;

    public void initConfig() {
        config = new ConfigUtil("CU", // entrust
                "/config", // path of config
                "config.mhf", // name of config
                "1.2", // version of config,
                false // empty config
        );
    }
}
```

these configs are set on demand

if you do not need to empty or version<br>
also can miss parameter in init<br>

like this

```java
import com.github.zhuaidadaya.utils.config.ConfigUtil;

public class Test {
    ConfigUtil config;

    public void initConfig() {
        config = new ConfigUtil("CU", // entrust
                "/config", // path of config
                "config.mhf" // name of config
        );
    }
}
```

every parameter is all can do this in init CU<br>
(but location of parameters is required)

## Set CU

CU maybe need set some options to use

here is options and explain

### path
path is file path location of config<br>
it can make by absolute or relative path

set it by ``` setPath(String) ``` or ``` setPath(File) ``` 

<details>
<summary>setPath</summary>

```java
import com.github.zhuaidadaya.utils.config.ConfigUtil;

public class Test {
    ConfigUtil config;

    public void initConfig() {
        config = new ConfigUtil();

        config.setPath("/config");
    }
}
```

</details>

<details>
<summary>setPath Reference </summary>

```java


public class ConfigUtil {
    public ConfigUtil setPath(String path) {
        addUtilConfig("path", path);
        return this;
    }

    public ConfigUtil setPath(File path) {
        return setPath(path.getPath());
    }
}
```

</details>

<hr>

### name
name is file name of config<br>

set it by ``` setName(String) ```

> name require a suffix <br>
> if did not have, will fix it to ".mhf" suffix

<details>
<summary>setName</summary>

```java
import com.github.zhuaidadaya.utils.config.ConfigUtil;

public class Test {
    ConfigUtil config;

    public void initConfig() {
        config = new ConfigUtil();

        config.setName("config.mhf");
    }
}
```

</details>

<details>
<summary>setName Reference </summary>

```java


public class ConfigUtil {
    public ConfigUtil setName(String name) {
        try {
            name.substring(name.indexOf("."), name.indexOf(".") + 1);
            addUtilConfig("name", name);
        } catch (Exception e) {
            addUtilConfig("name", name + (String.valueOf(name.charAt(name.length() - 1)).equals(".") ? "mhf" : ".mhf"));
        }
        return this;
    }
}
```

</details>

<hr>

### version
set it by ``` setVersion(String) ```

<details>
<summary>setVersion</summary>

```java
import com.github.zhuaidadaya.utils.config.ConfigUtil;

public class Test {
    ConfigUtil config;

    public void initConfig() {
        config = new ConfigUtil();

        config.setVersion("1.2");
    }
}
```

</details>

<details>
<summary>setVersion Reference </summary>

```java


public class ConfigUtil {
    public ConfigUtil setVersion(String version) {
        addUtilConfig("version", version);
        return this;
    }
}
```

</details>

<hr>

### entrust
entrust is a name of parent of config

set it by ``` setEntrust(String) ```

<details>
<summary>setEntrust</summary>

```java
import com.github.zhuaidadaya.utils.config.ConfigUtil;

public class Test {
    ConfigUtil config;

    public void initConfig() {
        config = new ConfigUtil();

        config.setEntrust("1.2");
    }
}
```

</details>

<details>
<summary>setEntrust Reference </summary>

```java


public class ConfigUtil {
    public ConfigUtil setEntrust(String entrust) {
        addUtilConfig("entrust", entrust);
        logger = LogManager.getLogger("ConfigUtil-" + entrust);
        return this;
    }
}
```

</details>

<hr>

### note
note is a note in config

set it by ``` setNote(String) ```

<details>
<summary>setNote</summary>

```java
import com.github.zhuaidadaya.utils.config.ConfigUtil;

public class Test {
    ConfigUtil config;

    public void initConfig() {
        config = new ConfigUtil();

        config.setNote("""
                This is a Example Note
                It can Wrap Lines
                to Note the Config
                """);
    }
}
```

</details>

<details>
<summary>setNote Reference </summary>

```java


public class ConfigUtil {
    public ConfigUtil setNote(String note) {
        addUtilConfig("note", note);
        return this;
    }
}
```

</details>

<hr>

### encryption
encryption the config

default encryption is "Composite Sequence"<br>
see [Encryption](#Encryption)

set it by ``` setEncryption(boolean) ```

<details>
<summary>setEncryption</summary>

```java
import com.github.zhuaidadaya.utils.config.ConfigUtil;

public class Test {
    ConfigUtil config;

    public void initConfig() {
        config = new ConfigUtil();

        config.setEncryption(true);
    }
}
```

</details>

<details>
<summary>setEncryption Reference </summary>

```java


public class ConfigUtil {
    public ConfigUtil setEncryption(boolean encryption) {
        addUtilConfig("encryption", encryption);
        if(getUtilBoolean("autoWrite")) {
            try {
                writeConfig();
            } catch (Exception e) {

            }
        }
        return this;
    }
}
```

</details>

<hr>

### encryption type
encryption type use in config

set it by ``` setEncryptionType(EncryptionType) ```

<details>
<summary>setEncryptionType</summary>

```java
import com.github.zhuaidadaya.utils.config.ConfigUtil;
import com.github.zhuaidadaya.utils.config.EncryptionType;

public class Test {
    ConfigUtil config;

    public void initConfig() {
        config = new ConfigUtil();

        config.setEncryptionType(EncryptionType.COMPOSITE_SEQUENCE);
    }
}
```

</details>

<details>
<summary>setEncryptionType Reference </summary>

```java


public class ConfigUtil {
    public ConfigUtil setEncryptionType(EncryptionType type) {
        this.encryptionType = type;
        return this;
    }
}
```

</details>

<hr>

### library offset
information of Composite Sequence

set it by ``` setLibraryOffset(int) ```

use -1 to become max in default<br>
use int value to custom library offset, no limit

> Warn <br>
> try not to set offset higher than 1024<br>
> if configs more than 500 entries

<details>
<summary>setLibraryOffset</summary>

```java
import com.github.zhuaidadaya.utils.config.ConfigUtil;

public class Test {
    ConfigUtil config;

    public void initConfig() {
        config = new ConfigUtil();

        config.setLibraryOffset(50);
    }
}
```

</details>

<details>
<summary>setLibraryOffset Reference </summary>

```java


public class ConfigUtil {
    public ConfigUtil setLibraryOffset(int offset) {
        if(offset != - 1)
            this.libraryOffset = Math.max(1, offset);
        else
            this.libraryOffset = 1024;
        return this;
    }
}
```

</details>

<hr>



## Encryption




## Reform

CU has a few reform support to use

### ConfigUtil

premier CU

### ReformUtil

### ReformPureUtil

<hr>