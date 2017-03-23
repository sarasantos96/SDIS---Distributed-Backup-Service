import java.io.BufferedReader;
import java.io.*;
import java.nio.file.*;
import java.nio.file.Path;
import java.nio.file.attribute.*;
import java.io.IOException;
import java.util.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import javax.xml.bind.DatatypeConverter;

public class Ft{
	public static final String INPUT_FILE_NAME = "pic.jpg";
	public static final String OUTPUT_FILE_NAME = "pic_OUTPUT.jpg";
	public static final int CHUNCK_SIZE = 64000;
	public static int n_chunks = 0;

	public static void main(String[] args)throws IOException{
		//exp1(); //read file line by line
		//exp2(); //read and print file metadata
		
		Vector<byte[]> ret;
		ret = splitFile();
		saveSplitFiles(ret);
		joinFiles();
		
		/*
		
		*/
	}

	public static void exp1(){
		File file = new File(INPUT_FILE_NAME);
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		    String line;
		    int i = 0;
		    while ((line = br.readLine()) != null && i < 20) {
		       System.out.println(line);
		       i++;
		    }
		}catch(Exception e){
			System.out.println(e.toString());
		}
	}

	public static void exp2() throws IOException{
		Path path = Paths.get(INPUT_FILE_NAME);
		BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
		System.out.println("path: " + System.getProperty("user.dir") + "/" + INPUT_FILE_NAME);
		System.out.println("size: " + attr.size());
		System.out.println("creationTime: " + attr.creationTime());
		System.out.println("lastAccessTime: " + attr.lastAccessTime());
		System.out.println("lastModifiedTime: " + attr.lastModifiedTime());

	}

	public static Vector<byte[]> splitFile(){
		Vector<byte[]> parts = new Vector<byte[]>();

		File input_file = new File(INPUT_FILE_NAME);
		FileInputStream file_input_stream;
		long file_size = input_file.length();
		int read = 0, n_bytes = CHUNCK_SIZE;
		byte[] byte_chunk;

		try{
			file_input_stream = new FileInputStream(input_file);
			while(file_size > 0){
				if(file_size <= CHUNCK_SIZE)
					n_bytes = (int) file_size;
				byte_chunk = new byte[n_bytes];
				read = file_input_stream.read(byte_chunk, 0, n_bytes);
				file_size = file_size - read;
				n_chunks++;
				parts.addElement(byte_chunk);
				byte_chunk = null;
			}
			file_input_stream.close();
		}catch(Exception e){
			System.out.println(e.getClass().getSimpleName());
			e.printStackTrace(new PrintStream(System.out));
		}

		System.out.println(parts.size());
		return parts;

	}

	public static void saveSplitFiles(Vector<byte[]> parts)throws IOException{
		String hashed = createHashedName();
		String new_file_name;
		FileOutputStream file_output_stream;

		for(int i = 0; i < parts.size(); i++){
			new_file_name = hashed + "_part_" + (i + 1) + ".chunk";
			try{
				file_output_stream = new FileOutputStream(new File(new_file_name));
				file_output_stream.write(parts.elementAt(i));
				file_output_stream.flush();
				file_output_stream.close();
				file_output_stream = null;
				
			}catch(Exception e){
				System.out.println(e.getClass().getSimpleName());
				e.printStackTrace(new PrintStream(System.out));
			}			
		}
	}

	public static void joinFiles() throws IOException{
		String hashed = createHashedName();
		File output_file = new File(OUTPUT_FILE_NAME);
		FileInputStream file_input_stream;
		FileOutputStream file_output_stream;
		int n_bytes_read = 0;
		byte[] input_bytes;

		try{
			file_output_stream = new FileOutputStream(output_file, true);
			for(int i = 0; i < n_chunks; i++){
				String input_file_name = hashed + "_part_" + (i+1) + ".chunk";
				File input_file = new File(input_file_name);
				file_input_stream = new FileInputStream(input_file);
				input_bytes = new byte[(int) input_file.length()];
				n_bytes_read = file_input_stream.read(input_bytes, 0, (int) input_file.length());
				file_output_stream.write(input_bytes);
				file_output_stream.flush();
				file_input_stream.close();
				file_input_stream = null;
				input_bytes = null;

			}
			file_output_stream.close();
			file_output_stream = null;
		}catch(Exception e){
			System.out.println(e.getClass().getSimpleName());
			e.printStackTrace(new PrintStream(System.out));
		}
	}

	public static String hash(String text){
		
		try{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
			String s = DatatypeConverter.printHexBinary(hash);
			//System.out.println(s);
			return s;
		}catch(Exception e){
			System.out.println(e.getClass().getSimpleName());
			e.printStackTrace(new PrintStream(System.out));
			return null;
		}

	}

	public static String createName() throws IOException{
		Path path = Paths.get(INPUT_FILE_NAME);
		BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
		long size = attr.size();
		FileTime modified_date = attr.lastModifiedTime();

		String string_to_hash = INPUT_FILE_NAME + size + modified_date;
		//System.out.println(string_to_hash);
		return string_to_hash;
	}

	public static String createHashedName() throws IOException{
		String s = createName(); 
		s = hash(s); 
		System.out.println(s);
		return s;
	}
}