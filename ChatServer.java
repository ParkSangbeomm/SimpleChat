//https://github.com/ParkSangbeomm/SimpleChat.git
import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor

	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				if(line.equals("/quit"))
					break;
				if(line.equals("/userlist")){// if user says "/userlist", then go to send_userlist methods.
					send_userlist();
			  }else if(line.contains("stupid")==true || line.contains("idiot")==true || line.contains("shit")==true || line.contains("hell")==true || line.contains("damn")==true){
					// if user types "stupid", "idiot", "shit", "hell",or "damn" in line, then go to badwords methods.
					badwords(line);
				}else if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}else
					broadcast(id + " : " + line);
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	//get the object which is this client's id
	//if user says badwords, then send warning message to id's PrintWriter buffer, and flush it.
	public void badwords(String s){
		Object obj = hm.get(id);
		PrintWriter pw = (PrintWriter)obj;
		pw.println("In your sentence, there is bad words.\nDo not speak like that.");
		pw.flush();
	}
	//if user types "/userlist", then this method runs.
	//get this user's id into Object 'obj' and ready to send buffer to PrintWriter
	//get all the HashMap's keys into collection 'c' and check it with iterator
	//In the while statements, run until iterator has next content
	//and print the ids that connect in server to user who types "/userlist"
	public void send_userlist(){

			Object obj = hm.get(id);
			PrintWriter pw = (PrintWriter)obj;
			Collection c=hm.keySet();
			Iterator it =c.iterator();
			while(it.hasNext()){
				pw.println("["+it.next()+"] is here.");
				pw.flush();
			}

	}
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to);
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){
		//get the HashMap's all keys to interator 'it'
		//get the all HashMap's values into 'iter' which is print buffer with each id's.
		//run while until iterator has hasNext
		//if 'it' has content which is same with typed user's name
		//then do not broadcast, just do nothing and go next
		synchronized(hm){
			Iterator<String> it = hm.keySet().iterator();
			Collection collection = hm.values();
			Iterator iter = collection.iterator();
			while(iter.hasNext()&&it.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next();
				if(it.next() != id){
					pw.println(msg);
					pw.flush();
				}
			}
		}
	} // broadcast
}
