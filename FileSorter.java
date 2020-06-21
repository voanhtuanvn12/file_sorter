package vn.zalo.kiki.admin.updater;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class FileSorter {
  private static int LIMIT_STRING = 1000;
  private static int numSplitFile;
  private static ArrayList<BufferedReader> listStream;

  public static class Pair{
    public String data;
    public BufferedReader br;
    public Pair(String data, BufferedReader br){
      this.data = data;
      this.br = br;
    }
  }

  public static void sort(String inputFile ,String outputFile){
    numSplitFile = 0;
    // read and splitfile (include sort)
    System.out.println("Reading and Splitting file........ ");
    if(readAndSplitFile(inputFile)){
      // if readAndSplitFile is succeeded
      // create output file
      System.out.println("Merge file........ ");
      try {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        // create a list of stream to all tmp file
        createListStream();
        // create a priorityQueue which each element contain a pair(String data, Stream);
        PriorityQueue<Pair> pq = new PriorityQueue<>(Comparator.comparing(o -> o.data));
        // add all stream with the first line in pq
        for(BufferedReader br : listStream){
          String line;
          if((line = br.readLine()) != null){
            pq.add(new Pair(line,br));
          }
        }
        // while loop
        while(pq.size() > 0){
          // get the first element
          Pair pair = pq.poll();
          // save to output file
          writer.write(pair.data);
          writer.newLine();
          // push the next line to pq
          String line;
          if((line = pair.br.readLine()) != null){
            pair.data = line;
            pq.add(pair);
          }else{
            pair.br.close();
          }
        }

        writer.close();
        // delete all tmp file
        deleteFile();

      }catch(Exception e){
        System.out.println("something wrong!!!");
        return;
      }
    }
  }

  public static void deleteFile(){
    for(int i = 0 ; i < numSplitFile; i++){
      String fileName = String.format("%d.tmp",i+1);
      File file = new File(fileName);
      if(file.delete()){
        System.out.println("Delete file "+fileName);
      }else{
        System.out.println("Cannot delete file "+fileName);
      }
    }
  }

  public static void createListStream(){
    try {
      listStream = new ArrayList<>();
      for(int i = 0 ; i < numSplitFile; i++){
        String fileName = String.format("%d.tmp",i+1);
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        listStream.add(br);
      }
    }catch(Exception e){
      System.out.println("something wrong!!!");
      return;
    }
  }


  public static boolean readAndSplitFile(String inputFile){
    File file = new File(inputFile);

    // one tmp file is alowed to store LIMIT_STRING lines
    String []data = new String[LIMIT_STRING];
    int sizeData = 0;


    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      // read line and store to data
      while ((line = br.readLine()) != null) {
        if(sizeData < LIMIT_STRING){
          data[sizeData] = line;
          sizeData++;
        }else{ // if data is full, store it to file
          // remember to sort data before saving
          Arrays.sort(data);
          // increase the numSplitFile
          numSplitFile ++;
          // store data to file
          // 1. create file name
          String fileOutName = String.format("%d.tmp",numSplitFile);
          // 2. create BufferedWriter
          BufferedWriter writer  = new BufferedWriter(new FileWriter(fileOutName));
          for(int i = 0 ; i < sizeData; i++){
            writer.write(data[i]);
            writer.write('\n');
          }
          writer.close();
          // 3.reset data
          sizeData = 0;
        }
      }
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  public static void main(String[] args) {
    sort("cities.txt","cities_sorted.txt");
  }
}
