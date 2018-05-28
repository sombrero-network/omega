package network.omega.ui.resource;

import cy.agorise.graphenej.Asset;
import org.json.JSONObject;

public class ResourceDescription {
    public String name;
    // installed OS
    public String descriptionOS;
    // installed packages list noticeable
    public String descriptionInstalled;
    // cores number
    public int numberOfCores;
    // 1 core min benchmark
    public int coreMinBenchmark;
    // ram size (512MB)
    public int ramSize;
    // ram minimum speed (5000MBs)
    public int ramMinSpeed;
    // disk size (15GB)
    public int diskSize;
    // minimal small files read/write speed (50MBs/45Mbs)
    public int minSmallFilesReadSpeed;
    public int minSmallFilesWriteSpeed;
    // minimal big files read/write speed (100MBs/80Mbs)
    public int minBigFilesReadSpeed;
    public int minBigFilesWriteSpeed;
    
    // host system requirements
    // free disk space (12.5GB)
    public float freeDiskRequired;
    // free ram (1.5GB)
    public float freeRamRequired;
    // min cpu cores
    public int minCPUCoresRequired;
    
    private String clean(String source) {
        return source.toLowerCase().replace("mb", "").replace("mbs", "").replace("gb", "").replace("s", "");
    }
    
    private String readJsonLineString(JSONObject mainJsonObj, String level1, String level2) {
        return (String) ((JSONObject) mainJsonObj.get(level1)).get(level2);
    }
    
    private int readJsonLineInt(JSONObject mainJsonObj, String level1, String level2) {
        return Integer.parseInt(clean((String) ((JSONObject) mainJsonObj.get(level1)).get(level2)));
    }
    
    private float readJsonLineFloat(JSONObject mainJsonObj, String level1, String level2) {
        return Float.parseFloat(clean((String) ((JSONObject) mainJsonObj.get(level1)).get(level2)));
    }
    
    public void parse(Asset a) {
        
        String jsonStr = a.getAssetOptions().getDescription();
        JSONObject obj = new JSONObject(jsonStr.replace("\\n", ""));
        if (obj instanceof JSONObject) {
            String mainJsonStr = (String) obj.get("main");
            JSONObject mainJsonObj = new JSONObject(mainJsonStr);
            descriptionOS = readJsonLineString(mainJsonObj, "de", "o");
            descriptionInstalled = readJsonLineString(mainJsonObj, "de", "i");
            
            numberOfCores = readJsonLineInt(mainJsonObj, "c", "c");
            coreMinBenchmark = readJsonLineInt(mainJsonObj, "c", "b");
            ramSize = readJsonLineInt(mainJsonObj, "r", "s");
            ramMinSpeed = readJsonLineInt(mainJsonObj, "r", "ms");
            diskSize = readJsonLineInt(mainJsonObj, "d", "s");
            
            String[] minSmallFiles = ((String) ((JSONObject) mainJsonObj.get("d")).get("msfs")).split("/");
            minSmallFilesReadSpeed = Integer.parseInt(clean(minSmallFiles[0]));
            minSmallFilesWriteSpeed = Integer.parseInt(clean(minSmallFiles[1]));
            String[] minBigFiles = ((String) ((JSONObject) mainJsonObj.get("d")).get("mbfs")).split("/");
            minBigFilesReadSpeed = Integer.parseInt(clean(minBigFiles[0]));
            minBigFilesWriteSpeed = Integer.parseInt(clean(minBigFiles[1]));
            
            freeDiskRequired = readJsonLineFloat(mainJsonObj, "h", "s");
            freeRamRequired = readJsonLineFloat(mainJsonObj, "h", "r");
            minCPUCoresRequired = readJsonLineInt(mainJsonObj, "h", "c");
            // System.out.println(this.toString());
            // System.out.println();
            name = descriptionOS + " - " + a.getSymbol().replace("RESOURCE", "");
        }
    }
    
    @Override
    public String toString() {
        return "descriptionOS='" + descriptionOS + '\'' + "\ndescriptionInstalled='" + descriptionInstalled + '\''
                + "\nnumberOfCores=" + numberOfCores + "\ncoreMinBenchmark=" + coreMinBenchmark + "\nramSize=" + ramSize
                + "\nramMinSpeed=" + ramMinSpeed + "\ndiskSize=" + diskSize + "\nminSmallFilesReadSpeed="
                + minSmallFilesReadSpeed + "\nminSmallFilesWriteSpeed=" + minSmallFilesWriteSpeed
                + "\nminBigFilesReadSpeed=" + minBigFilesReadSpeed + "\nminBigFilesWriteSpeed=" + minBigFilesWriteSpeed
                + "\nfreeDiskRequired=" + freeDiskRequired + "\nfreeRamRequired=" + freeRamRequired
                + "\nminCPUCoresRequired=" + minCPUCoresRequired;
    }
}
