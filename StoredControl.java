import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;


public class StoredControl{
  private String logFileName;
  private HashMap<String,Value> hmap;

  public StoredControl(String logFileName) throws IOException{
    this.hmap = new HashMap<String, Value>();
    this.logFileName = logFileName;
    File logFile = new File(logFileName);
    if(!logFile.exists())
      logFile.createNewFile();
    else
      loadHmap();

  }

  public class Value{
    public int replicationDeg;
    public ArrayList<String> peers;

    public Value(int replicationDeg, String[] peers){
      this.replicationDeg = replicationDeg;
      this.peers = new ArrayList<String>();
      if(peers != null)
        this.peers = new ArrayList<String>(Arrays.asList(peers));
    }

  }

  public void addNewLog(String chunkname, int replicationDeg, String[] peers) throws IOException{
    Value value = new Value(replicationDeg,peers);
    this.hmap.put(chunkname,value);
    this.saveHmap();
  }

  public void addNewPeer(String chunkname, String peer) throws IOException{
    Value value = this.hmap.get(chunkname);
    value.peers.add(peer);
    saveHmap();
  }

  public void loadHmap() throws IOException{
    File logfile = new File(this.logFileName);

		try {
			FileReader fileReader = new FileReader(logfile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
        String [] split = line.split(":");
        if(split.length == 3){
          String chunkname = split[0];
          int replicationDeg = Integer.parseInt(split[1]);
          String[] peers = split[2].split(",");
          this.addNewLog(chunkname,replicationDeg,peers);
        }else if(split.length == 2){
          String chunkname = split[0];
          int replicationDeg = Integer.parseInt(split[1]);
          String[] peers = null;
          this.addNewLog(chunkname,replicationDeg,peers);
        }
			}
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
  }

  public void saveHmap() throws IOException{
    String content = new String("");
    for(Map.Entry<String, Value> entry: this.hmap.entrySet()) {
        String chunkname = entry.getKey();
        int replicationDeg = entry.getValue().replicationDeg;
        ArrayList<String> peers = entry.getValue().peers;
        content += chunkname+":"+replicationDeg+":";
        for(int i=0; i< peers.size();i++){
          content+=peers.get(i)+",";
        }
        content += "\n";
    }
    byte data[] = content.trim().getBytes();
    FileOutputStream out = new FileOutputStream(this.logFileName);
    out.write(data);
    out.close();
  }

 /*public void updateRepDeg(String chunckname) throws IOException{
    Value oldvalue = this.hmap.get(chunckname);
    this.addNewLog(oldvalue.filename,chunckname,oldvalue.replicationDeg,oldvalue.atualReplicationDeg + 1);
  }*/

/*  public int getRepDeg(String chunckname){
    return this.hmap.get(chunckname).replicationDeg;
  }
*/
/*  public int getAtualRepDeg(String chunckname){
    return this.hmap.get(chunckname).atualReplicationDeg;
  }*/

  public boolean isChunkStored(String chunckname){
    Value value = this.hmap.get(chunckname);
    System.out.println(this.hmap.size());

    if(value != null)
      return true;

    return false;
  }

  public boolean isPeerStored(String chunkname,String peer){
    ArrayList<String> peers = hmap.get(chunkname).peers;
    int index = peers.indexOf(peer);
    if(index != -1)
      return true;
    return false;
  }

  /*public void deleteAllEntries(String fileId) throws IOException{

    for(Iterator<Map.Entry<String, Value>> i = this.hmap.entrySet().iterator(); i.hasNext(); ){
      Map.Entry<String, Value> entry = i.next();
      String chunkname = entry.getKey();
      String entryfileId = chunkname.split("_")[0];

      if(entryfileId.equals(fileId)){
        i.remove();
      }
    }
    saveHmap();
  }*/

  public HashMap<String,Value> getHMap(){
    return hmap;
  }
}
