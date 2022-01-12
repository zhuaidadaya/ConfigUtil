# ConfigUtil

a config for java(≥16)

here we named ConfigUtil to "CU" for convenient

# Use CU

## Initialization

a simple CU

```java
import com.github.zhuaidadaya.config.utils.ConfigUtil;

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
import com.github.zhuaidadaya.config.utils.ConfigUtil;

public class Test {
    ConfigUtil config;

    public void initConfig() {
        config = new ConfigUtil(
                "CU", // entrust
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
import com.github.zhuaidadaya.config.utils.ConfigUtil;

public class Test {
    ConfigUtil config;

    public void initConfig() {
        config = new ConfigUtil(
                "CU", // entrust
                "/config", // path of config
                "config.mhf" // name of config
        );
    }
}
```

every parameter is all can do this in init CU<br>
(but location of parameters is required)

