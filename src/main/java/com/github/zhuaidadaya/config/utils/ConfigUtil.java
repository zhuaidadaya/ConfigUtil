package com.github.zhuaidadaya.config.utils;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;


public class ConfigUtil {
    /**
     *
     */
    private final Object2ObjectMap<Object, Config<Object, Object>> configs = new Object2ObjectArrayMap<>();
    private EncryptionType encryptionType = EncryptionType.COMPOSITE_SEQUENCE;
    /**
     *
     */
    private Logger logger  = LogManager.getLogger("ConfigUtil");
    /**
     * if true
     * run <code>writeConfig()</code> when config has updated
     */
    private boolean empty = false;
    private int splitRange = 20;
    private int libraryOffset = 5;
    private boolean canShutdown = true;
    private boolean shuttingDown = false;
    private boolean shutdown = false;

    public ConfigUtil() {
        defaultUtilConfigs();
        readConfig(true);
    }

    public ConfigUtil(String entrust) {
        defaultUtilConfigs();
        logger = LogManager.getLogger("ConfigUtil-" + entrust);
        readConfig(true);
    }

    public ConfigUtil( String entrust, String configPath) {
        defaultUtilConfigs();
        setPath(configPath);
        logger = LogManager.getLogger("ConfigUtil-" + entrust);
        readConfig(true);
    }

    public ConfigUtil(String entrust, String configPath, String configName) {
        defaultUtilConfigs();
        setPath(configPath);
        setName(configName);
        setEntrust(entrust);
        logger = LogManager.getLogger("ConfigUtil-" + entrust);
        readConfig(true);
    }

    public ConfigUtil(String entrust, String configPath, String configName, String configVersion) {
        defaultUtilConfigs();
        setPath(configPath);
        setName( configName);
        setVersion(configVersion);
        setEntrust(entrust);
        logger = LogManager.getLogger("ConfigUtil-" + entrust);
        readConfig(true);
    }

    public ConfigUtil( String entrust, String configPath, String configName, String configVersion, boolean empty) {
        defaultUtilConfigs();
        setPath(configPath);
        setName(configName);
        setVersion(configVersion);
        setEntrust(entrust);
        logger = LogManager.getLogger("ConfigUtil-" + entrust);
        this.empty = empty;
        if(! empty)
            readConfig(true);
    }

    public static void main(String[] args) {
        ConfigUtil config = new ConfigUtil("config/", "test.mhf", "1.1", "CU").setEncryptionType(EncryptionType.COMPOSITE_SEQUENCE) //
                .setLibraryOffset(50) //
                .setSplitRange(5000) //
                .setEncryption(true) //
                .setEncryptionHead(true) //
                .setInseparableLevel(3); //
        //                ;

        Logger logger = LogManager.getLogger("Teat");

        int count = 0;
        while(true) {
            try {
                count++;
                long startTime = System.currentTimeMillis();
                config.readConfig();
                logger.info("read done in " + (System.currentTimeMillis() - startTime) + "ms, load " + config.getConfigTotal() + " configs");
                config.set("test" + count, "teeeeeeeeeeeeeeeeeeeeeeeeest" + count);
                config.writeConfig();
                logger.info("write done in " + (System.currentTimeMillis() - startTime) + "ms, load " + config.getConfigTotal() + " configs");

                if(count >500) {
                    config.shutdown();

                    break;
                }
            } catch (Exception e) {
                logger.info("test failed after " + count + ", CU have " + config.getConfigTotal() + " configs");
            }
        }
    }

    public static ConfigUtil emptyConfigUtil() {
        return new ConfigUtil(null, null, "1.1", null, true);
    }

    public ConfigUtil setPath(String path) {
        addUtilConfig("path", path);
        return this;
    }

    public ConfigUtil setVersion(String version) {
        addUtilConfig("version", version);
        return this;
    }

    public ConfigUtil setName(String name) {
        try {
            name.substring(name.indexOf("."), name.indexOf(".") + 1);
        } catch (Exception e) {
            addUtilConfig("name", name + (String.valueOf(name.charAt(name.length() - 1)).equals(".") ? "mhf" : ".mhf"));
        }
        return this;
    }

    public ConfigUtil setPath(File path) {
        return setPath(path.getPath());
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

    public void addUtilConfig(Object name, Object value) {
        configs.put("CU%" + name, new Config<>("CU%" + name, value, false));
    }

    public ConfigUtil setInseparableLevel(int inseparableLevel) {
        configs.put("CU%inseparableLevel", new Config<>("CU%inseparableLevel", inseparableLevel > - 1 ? inseparableLevel < 4 ? inseparableLevel : 3 : 0, false));
        return this;
    }

    public ConfigUtil setLibraryOffset(int offset) {
        if(offset != - 1)
            this.libraryOffset = Math.max(1, offset);
        else
            this.libraryOffset = 1024;
        return this;
    }

    public ConfigUtil setSplitRange(int range) {
        splitRange = range;
        return this;
    }

    public ConfigUtil setEncryptionType(EncryptionType type) {
        this.encryptionType = type;
        return this;
    }

    public ConfigUtil setEmpty(boolean empty) {
        this.empty = empty;
        return this;
    }

    public ConfigUtil setEntrust(String entrust) {
        addUtilConfig("entrust", entrust);
        logger = LogManager.getLogger("ConfigUtil-" + entrust);
        return this;
    }

    public ConfigUtil setEncryptionHead(boolean encryptionHead) {
        addUtilConfig("encryptionHead", encryptionHead);
        return this;
    }

    public ConfigUtil setAutoWrite(boolean autoWrite) {
        configs.put("CU%autoWrite", new Config<>("CU%autoWrite", autoWrite, false));
        return this;
    }

    public ConfigUtil setNote(String note) {
        addUtilConfig("note", note);
        return this;
    }

    public ConfigUtil fuse(ConfigUtil parent) {
        for(Object o : parent.configs.keySet())
            this.setConf(true, o.toString(), parent.configs.get(o.toString()).getValue());
        return this;
    }

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

    public Map<Object, Config<Object, Object>> getConfigs() {
        return configs;
    }

    public Config<Object, Object> getConfig(Object conf) {
        return configs.get(conf);
    }

    public boolean readConfig() {
        return readConfig(false);
    }

    public boolean readConfig(boolean log) {
        return readConfig(log, false);
    }

    public boolean readConfig(boolean log, boolean forceLoad) {
        if(shuttingDown) {
            return false;
        }
        canShutdown = false;

        if(empty) {
            canShutdown = true;

            return false;
        }
        int configSize = 0;
        try {
            if(log)
                logger.info("loading config from: " + getUtilString("name"));

            File configFile = new File(getUtilString("path") + "/" + getUtilString("name"));

            BufferedReader br = new BufferedReader(new FileReader(configFile, Charset.forName("unicode")));
            StringBuilder builder = decryption(br, forceLoad);

            br.close();

            JSONObject source = new JSONObject(builder.toString());
            JSONArray configs = source.getJSONArray("configs");
            configSize = builder.length();

            if(log)
                logger.info("loading configs");

            for(Object o : configs) {
                JSONObject config = new JSONObject(o.toString());
                String configKey = config.keySet().toArray()[0].toString();
                if(log)
                    logger.info("loading for config: " + configKey);
                JSONObject configDetailed = config.getJSONObject(configKey);
                if(configDetailed.getBoolean("listTag")) {
                    JSONArray array = configDetailed.getJSONArray("values");
                    ObjectArraySet<Object> addToConfig = new ObjectArraySet<>();
                    for(Object inArray : array)
                        addToConfig.add(inArray);
                    setListConf(true, configKey, addToConfig);
                } else {
                    setConf(true, configKey, configDetailed.get("value").toString());
                }
            }

            if(log)
                logger.info("loading manifest");

            JSONObject manifest = source.getJSONObject("manifest");
            for(String s : manifest.keySet()) {
                addUtilConfig(s, manifest.get(s));
            }

            if(log)
                logger.info("load config done");

            canShutdown = true;

            return true;
        } catch (IllegalArgumentException e) {
            canShutdown = true;

            throw e;
        } catch (Exception e) {
            if(! shuttingDown) {
                e.printStackTrace();
                logger.error(empty ? ("failed to load config") : ("failed to load config: " + getUtilString("name")));
                if(! empty) {
                    File configFile = new File(getUtilString("path") + "/" + getUtilString("name"));
                    if(! configFile.isFile() || configFile.length() == 0 || configSize == 0) {
                        try {
                            configFile.getParentFile().mkdirs();
                            configFile.createNewFile();
                            writeConfig();
                            logger.info("created new config file for " + getUtilString("entrust"));
                        } catch (Exception ex) {
                            logger.error("failed to create new config file for " + getUtilString("entrust"));
                        }
                    }
                }
            }

            canShutdown = true;

            return false;
        }
    }

    public StringBuilder decryption() {
        try {
            File configFile = new File(getUtilString("path") + "/" + getUtilString("name"));

            BufferedReader br = new BufferedReader(new FileReader(configFile, Charset.forName("unicode")));

            return decryption(br, false);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {

        }

        return null;
    }

    public StringBuilder decryption(BufferedReader reader, boolean forceLoad) {
        try {
            StringBuilder builder = new StringBuilder();
            String cache = reader.readLine();
            if(cache == null) {
                return null;
            }
            int encryptionType = cache.chars().toArray()[0];
            String encryptionEnable = cache.substring(2);
            boolean encrypted = encryptionEnable.startsWith("encryption") | encryptionEnable.startsWith("MCH DB");
            if(encrypted) {
                switch(encryptionType) {
                    case 0 -> {
                        while((cache = reader.readLine()) != null) {
                            if(! cache.startsWith("/**") & ! cache.startsWith(" *") & ! cache.startsWith(" */")) {
                                if(cache.length() > 0)
                                    builder.append(cache).append("\n");
                            }
                        }

                        int checkCode = Integer.parseInt(String.valueOf(builder.chars().toArray()[0]));

                        BufferedReader configRead = new BufferedReader(new StringReader(builder.toString()));

                        StringBuilder s1 = new StringBuilder();
                        while((cache = configRead.readLine()) != null) {
                            int lim = cache.length() > 1 ? cache.chars().toArray()[0] : 0;

                            boolean checkSkip = false;

                            for(Object o : cache.chars().toArray()) {
                                if(checkSkip) {
                                    int details = Integer.parseInt(o.toString());
                                    if(details != 10) {
                                        s1.append((char) (details - lim - checkCode));
                                    }
                                }
                                checkSkip = true;
                            }
                        }

                        return s1;
                    }
                    case 1 -> {
                        while((cache = reader.readLine()) != null) {
                            if(! cache.startsWith("/**") & ! cache.startsWith(" *") & ! cache.startsWith(" */")) {
                                if(cache.length() > 0)
                                    builder.append(cache).append("\n");
                            }
                        }

                        int checkCode = Integer.parseInt(String.valueOf(builder.chars().toArray()[0]));

                        BufferedReader configRead = new BufferedReader(new StringReader(builder.toString()));

                        while((cache = configRead.readLine()) != null) {
                            if(cache.startsWith("LIBRARY ")) {
                                break;
                            }
                        }

                        Int2IntMap libraryMap = new Int2IntOpenHashMap();

                        StringBuilder libraryInformation = new StringBuilder();

                        while((cache = configRead.readLine()) != null) {
                            if(cache.startsWith("INFORMATION ")) {
                                break;
                            }
                            libraryInformation.append(cache.replace("\b", "\n")).append("\n");
                        }

                        BufferedReader libraryRead = new BufferedReader(new StringReader(libraryInformation.toString()));

                        while((cache = libraryRead.readLine()) != null) {
                            int headCode = cache.chars().toArray()[0];
                            String[] libraryLine = cache.substring(1).split("\t");
                            for(String s : libraryLine) {
                                StringBuilder charCode = new StringBuilder();
                                int signCode = - 1;
                                boolean in = false;
                                for(int i : s.chars().toArray()) {
                                    if(! in) {
                                        signCode = i;
                                        in = true;
                                        continue;
                                    }
                                    charCode.append((char) (i - checkCode - headCode));
                                }
                                if(! charCode.toString().equals(""))
                                    libraryMap.put(signCode, Integer.parseInt(charCode.toString()));
                            }
                        }

                        libraryRead.close();

                        StringBuilder information = new StringBuilder();

                        while((cache = configRead.readLine()) != null) {
                            information.append(cache.substring(1));
                        }

                        information = new StringBuilder(information.toString().replace("\t", ""));

                        StringBuilder recodeInformation = new StringBuilder();

                        try {
                            for(int i : information.chars().toArray()) {
                                recodeInformation.append((char) libraryMap.get(i));
                            }
                        } catch (Exception e) {

                        }

                        return recodeInformation;
                    }
                    default -> {
                        if(! forceLoad)
                            throw new IllegalArgumentException("unsupported encryption type: " + encryptionType);
                    }
                }

            } else {
                while(true) {
                    String startWith = reader.readLine();
                    if(startWith.replace(" ", "").startsWith("{")) {
                        builder.append(startWith);
                        break;
                    }
                }
                while((cache = reader.readLine()) != null) {
                    if(! cache.startsWith("/**") || cache.startsWith(" *") || cache.startsWith(" */"))
                        builder.append(cache);
                }

                return builder;
            }
        } catch (IOException e) {

        }

        return null;
    }

    public void writeConfig() throws Exception {
        if(shuttingDown) {
            return;
        }

        canShutdown = false;

        BufferedWriter writer = new BufferedWriter(new FileWriter(getUtilString("path") + "/" + getUtilString("name"), Charset.forName("unicode"), false));

        StringBuilder write = new StringBuilder(this.toJSONObject().toString());

        Random r = new Random();

        if(getUtilBoolean("encryption")) {
            switch(encryptionType.getId()) {
                case 0 -> {
                    StringBuffer buffer = encryptionByRandomSequence(write, r);
                    write(writer, buffer);
                    writer.close();
                }
                case 1 -> {
                    StringBuffer buffer = encryptionByCompositeSequence(write);
                    write(writer, buffer);
                    writer.close();
                }
            }
        } else {
            StringBuffer buffer = new StringBuffer();
            buffer.append("no encryption config: [config_size=").append(write.length()).append(", config_version=").append(getUtilString("version")).append("]");
            buffer.append("\n");
            buffer.append(write);
            write(writer, buffer);
            writer.close();
        }

        canShutdown = true;
    }

    public void write(Writer writer, StringBuffer information) throws IOException {
        writer.write(information.toString());
    }

    public void write(Writer writer, String information) throws IOException {
        write(writer, new StringBuffer(information));
    }

    public void write(StringBuffer information) throws IOException {
        write(information.toString());
    }

    public void write(String information) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(getUtilString("path") + "/" + getUtilString("name"), Charset.forName("unicode"), false));
        write(writer, new StringBuffer(information));
        writer.close();
    }

    public StringBuffer encryptionByRandomSequence(StringBuilder write, Random r) {
        int checkingCodeRange = r.nextInt(1024 * 8);
        checkingCodeRange = checkingCodeRange > 13 ? checkingCodeRange : 14;
        int checkingCode = r.nextInt(checkingCodeRange);
        checkingCode = checkingCode > 13 ? checkingCode : 14;
        int split = 0;

        StringWriter writer = new StringWriter();

        if(getUtilBoolean("encryption")) {
            int wrap = splitRange;

            for(; wrap > 0; wrap--) {
                int splitIndex = r.nextInt(100);
                if(splitIndex < 50) {
                    splitIndex += 50;
                }
                if((splitIndex + split) < write.length()) {
                    split += splitIndex - 1;
                    write.insert(split, "\n");
                } else {
                    break;
                }
            }
        }

        int[] charArray = write.chars().toArray();

        writer.write(0);

        if(! getUtilBoolean("encryptionHead")) {
            writer.write(" encryption: [" + "type=" + encryptionType.getName() + ", ");
            writer.write("SUPPORT=MCH -> https://github.com/zhuaidadaya/ConfigUtil , ");
            writer.write("check code=" + checkingCode + ", ");
            writer.write("offset=" + checkingCodeRange + ", ");
            writer.write("config size=" + write.length() + ", ");
            writer.write("config version=" + getUtilString("version") + ", ");
            writer.write("split=" + split + ", ");
            writer.write("split range=" + splitRange + "]");
            writer.write(10);
            writer.write(formatNote());
            writer.write(10);
        } else {
            writer.write(" MCH DB   ");
            write3RandomByte(writer);
            writer.write(" TYPE?" + encryptionType.getName() + "  ");
            write3RandomByte(writer);
            writer.write(" SUPPORT?" + "MCH -> https://github.com/zhuaidadaya/ConfigUtil  ");
            write2RandomByte(writer);
            writer.write(" OFFSET?" + checkingCodeRange);
            write3RandomByte(writer, checkingCodeRange);
            writer.write(" VER?" + getUtilString("version"));
            write2RandomByte(writer, checkingCodeRange);
            writer.write(" EC?" + checkingCode);
            write2RandomByte(writer, checkingCodeRange);
            writer.write(" SZ?" + write.length());
            write3RandomByte(writer, checkingCodeRange);
            writer.write("\n");
        }

        writer.write(checkingCodeRange);
        writer.write("\n");
        writer.write(checkingCode);

        int count = 0;
        for(Object o : charArray) {
            count++;
            if(Integer.parseInt(o.toString()) == 10) {
                int rand = r.nextInt(checkingCodeRange);
                writer.write(10);
                checkingCode = rand > 13 ? rand : 14;
                if(count != charArray.length)
                    writer.write(checkingCode);
            } else {
                writer.write((Integer.parseInt(o.toString()) + checkingCode + checkingCodeRange));
            }
        }

        return writer.getBuffer();
    }

    public StringBuffer encryptionByCompositeSequence(StringBuilder write) {
        Random r = new Random();
        int checkingCodeRange = 1024 * 12;
        int checkingCode = r.nextInt(checkingCodeRange);
        checkingCode = checkingCode > 13 ? checkingCode : 14;
        int[] charArray = write.chars().toArray();

        StringWriter writer = new StringWriter();

        writer.write(1);

        if(! getUtilBoolean("encryptionHead")) {
            writer.write(" encryption: [");
            writer.write("type=" + encryptionType.getName() + ", ");
            writer.write("SUPPORT=MCH -> https://github.com/zhuaidadaya/ConfigUtil , ");
            writer.write("check code=" + checkingCode + ", ");
            writer.write("offset=" + checkingCodeRange + ", ");
            writer.write("config size=" + write.length() + ", ");
            writer.write("config version=" + getUtilString("version"));
            writer.write("]");
            writer.write(10);
            writer.write(formatNote());
            writer.write(10);
        } else {
            writer.write(" MCH DB   ");
            write2RandomByte(writer);
            writer.write(" TYPE?" + encryptionType.getName() + "  ");
            write3RandomByte(writer);
            writer.write(" SUPPORT?" + "MCH -> https://github.com/zhuaidadaya/ConfigUtil  ");
            write3RandomByte(writer);
            writer.write(" OFFSET?" + checkingCodeRange);
            write2RandomByte(writer);
            writer.write(" VER?" + getUtilString("version"));
            write2RandomByte(writer);
            writer.write(" SZ?" + write.length());
            write3RandomByte(writer);
            writer.write(10);
        }

        writer.write(checkingCode);
        writer.write(10);
        writer.write("LIBRARY ");
        write2RandomByte(writer);
        writer.write("  ");
        write3RandomByte(writer);
        writer.write("\n");

        Object2IntMap<String> libraryMap = new Object2IntArrayMap<>();
        Int2IntMap libraryOffsetIndex = new Int2IntArrayMap();

        int count = 0;
        int lim = r.nextInt(checkingCodeRange);
        int head = lim > 13 ? lim : 14;
        int split = 50;
        writer.write(head);

        int offset;
        int inseparableLevel = getUtilInt("inseparableLevel");

        //  generate library
        for(Object o : charArray) {
            offset = 0;

            int sourceChar = Integer.parseInt(o.toString());

            try {
                if(libraryOffsetIndex.get(sourceChar) > libraryOffset - 1) {
                    continue;
                }
            } catch (Exception e) {

            }

            int writeChar = sourceChar + checkingCode + head;

            boolean dump = false;

            //            while(libraryMap.containsKey(sourceChar + "-" + offset)) {
            //                dump = true;
            try {
                offset = libraryOffsetIndex.get(sourceChar) + 1;
            } catch (Exception e) {

            }
            //                if(offset > libraryOffset - 1) {
            //                    next = true;
            //                    break;
            //                }
            //            }

            count++;

            while(libraryMap.containsValue(writeChar)) {
                dump = true;
                head++;
                writeChar = sourceChar + checkingCode + head;
            }

            if(dump) {
                writer.write("\b");
                writer.write(head);
            }

            if(count > split) {
                writer.write(10);
                switch(inseparableLevel) {
                    case 0 -> split = r.nextInt(15);
                    case 1 -> split = r.nextInt(30);
                    case 2 -> split = r.nextInt(50);
                }
                count = 0;
                lim = r.nextInt(checkingCodeRange);
                head = lim > 13 ? lim : 14;
                writer.write(head);
            }

            writer.write(writeChar);

            for(Object o2 : o.toString().chars().toArray()) {
                writer.write(Integer.parseInt(o2.toString()) + checkingCode + head);
            }

            if(libraryMap.containsKey(sourceChar + "-0")) {
                libraryOffsetIndex.put(sourceChar, offset);
                libraryMap.put(sourceChar + "-" + offset, writeChar);
            } else {
                libraryOffsetIndex.put(sourceChar, 0);
                libraryMap.put(sourceChar + "-0", writeChar);
            }

            writer.write("\t");
        }

        StringBuilder writeInformation = new StringBuilder();

        for(int c : charArray) {
            if(libraryOffsetIndex.get(c) == 0)
                writeInformation.append((char) libraryMap.get(c + "-0").intValue());
            else
                writeInformation.append((char) libraryMap.get(c + "-" + r.nextInt(libraryOffsetIndex.get(c))).intValue());
        }

        writer.write("\n");
        write2RandomByte(writer);
        writer.write("\n");
        writer.write("INFORMATION ");
        write2RandomByte(writer);
        writer.write("   ");
        write3RandomByte(writer);
        writer.write("\n");

        int tabCount = 0;
        int tab = r.nextInt(15);
        count = 0;
        lim = r.nextInt(checkingCodeRange);
        head = lim > 13 ? lim : 14;
        split = 300;
        writer.write(head);
        for(int c : writeInformation.chars().toArray()) {
            count++;
            tabCount++;
            if(count > split) {
                writer.write(10);
                switch(inseparableLevel) {
                    case 0 -> split = r.nextInt(300);
                    case 1 -> split = Math.max(150, r.nextInt(300));
                    case 2 -> split = Math.max(200, r.nextInt(300));
                    case 3 -> split = 300;
                }
                count = 0;
                lim = r.nextInt(checkingCodeRange);
                head = lim > 13 ? lim : 14;
                writer.write(head);
            } else if(tabCount > tab & ! (inseparableLevel == 3)) {
                writer.write("\t");
                switch(inseparableLevel) {
                    case 0 -> tab = r.nextInt(15);
                    case 1 -> tab = r.nextInt(30);
                    case 2 -> tab = r.nextInt(50);
                }
                tabCount = 0;
            }

            try {
                writer.write(c);
            } catch (Exception e) {

            }
        }

        return writer.getBuffer();
    }

    public void writeRandomByte(Writer writer, int limit, int bytes) {
        Random r = new Random();
        try {
            for(int i = 0; i < bytes; i++) {
                int next = r.nextInt(limit);
                writer.write(next > 13 ? next : 14);
            }
        } catch (Exception e) {

        }
    }

    public void write3RandomByte(Writer writer, int limit) {
        writeRandomByte(writer, limit, 3);
    }

    public void write3RandomByte(Writer writer) {
        write3RandomByte(writer, new Random().nextInt(25565));
    }

    public void write2RandomByte(Writer writer, int limit) {
        writeRandomByte(writer, limit, 2);
    }

    public void write2RandomByte(Writer writer) {
        write2RandomByte(writer, new Random().nextInt(25565));
    }

    public ConfigUtil set(Object key, Object... configKeysValues) throws IllegalArgumentException {
        setConf(false, key, configKeysValues);
        return this;
    }

    public ConfigUtil setConf(boolean init, Object key, Object... configKeysValues) throws IllegalArgumentException {
        if(configKeysValues.length > 1 & configKeysValues.length % 2 != 0)
            throw new IllegalArgumentException("values argument size need Integral multiple of 2, but argument size " + configKeysValues.length + " not Integral multiple of 2");
        configs.put(key, new Config<>(key, configKeysValues, false));
        if(getUtilBoolean("autoWrite") & ! init) {
            try {
                writeConfig();
            } catch (Exception e) {

            }
        }
        return this;
    }

    public ConfigUtil setList(Object key, Object... configValues) {
        setListConf(false, key, configValues);
        return this;
    }

    public ConfigUtil setListConf(boolean init, Object key, Object... configValues) {
        configs.put(key, new Config<>(key, configValues, true));
        if(getUtilBoolean("autoWrite") & ! init) {
            try {
                writeConfig();
            } catch (Exception e) {

            }
        }
        return this;
    }


    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(Object o : configs.keySet()) {
            builder.append(o.toString()).append("=").append(configs.get(o).toString()).append(", ");
        }

        try {
            builder.replace(builder.length() - 2, builder.length(), "");
        } catch (Exception e) {

        }

        return "ConfigUtil(" + builder + ")";
    }

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        JSONArray addToConfig = new JSONArray();
        for(Object o : configs.keySet()) {
            addToConfig.put(configs.get(o.toString()).toJSONObject());
        }

        json.put("configs", addToConfig);

        JSONObject manifest = new JSONObject();
        manifest.put("configVersion", getUtilString("version"));
        manifest.put("configsTotal", configs.size());
        manifest.put("encryption", getUtilBoolean("encryption"));
        manifest.put("encryptionHead", getUtilBoolean("encryptionHead"));
        manifest.put("config", new File(getUtilString("path") + "/" + getUtilString("name")));
        manifest.put("autoWrite", getUtilBoolean("autoWrite"));
        json.put("manifest", manifest);

        return json;
    }

    public boolean equal(ConfigUtil configUtil1, ConfigUtil configUtil2) {
        return configUtil1.toString().equals(configUtil2.toString());
    }

    public boolean equal(ConfigUtil configUtil) {
        return configUtil.toString().equals(this.toString());
    }

    public String formatNote() {
        if(getUtilString("note") != null) {
            try {
                BufferedReader reader = new BufferedReader(new StringReader(getUtilString("note")));
                StringBuilder builder = new StringBuilder("/**\n");

                String cache;
                while((cache = reader.readLine()) != null)
                    builder.append(" * ").append(cache).append("\n");
                builder.append(" */");

                return builder.toString();
            } catch (Exception e) {
                return "";
            }
        } else {
            return "";
        }
    }

    public boolean canShutdown() {
        return canShutdown;
    }

    public void setShuttingDown(boolean shuttingDown) {
        this.shuttingDown = shuttingDown;
    }

    public void shutdown() {
        logger.info("saving configs and shutting down ConfigUtil");
        try {
            while(! canShutdown()) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {

                }
            }

            writeConfig();

            logger.info("all config are saved, shutting down");
        } catch (Exception e) {
            logger.error("failed to save configs, shutting down");
        }
        setShuttingDown(true);
        while(! canShutdown()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }
        shutdown = true;
        logger.info("ConfigUtil are shutdown");
    }

    public int getConfigTotal() {
        return configs.size();
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public String getConfigString(Object config) {
        return getConfig(config).getString();
    }

    public int getConfigInt(Object config) {
        return getConfig(config).getInt();
    }

    public long getConfigLong(Object config) {
        return getConfig(config).getLong();
    }

    public boolean getConfigBoolean(Object config) {
        return getConfig(config).getBoolean();
    }

    public JSONObject getConfigJSONObject(Object config) {
        return getConfig(config).getJSONObject();
    }

    public JSONArray getConfigJSONArray(Object config) {
        return getConfig(config).getJSONArray();
    }

    public String getUtilString(Object config) {
        return getConfig("CU%" + config).getString();
    }

    public boolean getUtilBoolean(Object config) {
        return getConfig("CU%" + config).getBoolean();
    }

    public int getUtilInt(Object config) {
        return getConfig("CU%" + config).getInt();
    }

    public long getUtilLong(Object config) {
        return getConfig("CU%" + config).getLong();
    }

    public JSONObject getUtilJSONObject(Object config) {
        return getConfig("CU%" + config).getJSONObject();
    }

    public JSONArray getUtilJSONArray(Object config) {
        return getConfig("CU%" + config).getJSONArray();
    }
}

