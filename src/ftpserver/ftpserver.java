package ftpserver;

import java.net.*;
import java.io.*;
public class ftpserver
{
  ThreadGroup group;
	ftpserver()
	{
		group=new ThreadGroup("FTPThreads");
		
	}
	protected void finalize()
	{
	  System.out.println("*******************�������ѹر�*******************");
		group.stop();
	}
  public static void main(String args[])
 {
    ServerSocket server=null;
    Socket user=null;
    ftpserver ftp=new ftpserver();
    try
    {
      server=new ServerSocket(21);
    }
    catch(IOException e)
    {
      System.out.println("ERRO:"+e);
    }
    System.out.println("*******************������������*******************");    
    while(true)
    {
     try
     {
       user=server.accept();
       (new ftpserverThread(ftp.group,ftp,user)).start();
     }
     catch(IOException e)
     {
    	 System.out.println(""+e);
     }
    }
  }
}
class ftpserverThread extends Thread
{
	ftpserver ftp;
	Socket user;
	DataOutputStream out=null;
	DataInputStream in=null;
	String line;
	String user_name,password,filepath,path;
	File file;
  
	public ftpserverThread(ThreadGroup group,ftpserver ftp,Socket socket)
	{
		super(group,"FTPThread");
		this.ftp=ftp;
		this.user=socket;
		filepath=("d:/");
	}
	public void display()
	{
	  try
	  {
	     path=in.readUTF();
       if(path.equals("."))
       {
         file=new File(filepath);  
         path=file+""; 
       }
       else
         {
          file=new File(new String(filepath+"\\"+path));   
          path=filepath+"\\"+path;        
         }     
       if(file.isDirectory())
       {
          File tem;
          String list[]=file.list();               
          out.writeInt(list.length);
          for(int i=0;i<list.length;i++)
            {
               tem=new File(path+"\\"+list[i]);
               out.writeUTF(list[i]); 
               if(tem.isDirectory()){
            	   out.writeUTF("�ļ���");
            	   out.writeUTF("  ");
            	   }
               else {
            	   out.writeUTF("�ļ�");
            	   out.writeUTF(""+tem.length());
            	   }                                                       
               out.writeUTF(""+tem.lastModified());
            }               
       }
       else if(path.equals("."))
          {
            file.mkdir();
            out.writeInt(0);
          }
         else
         {
           out.writeInt(0);
           out.writeUTF("·������ȷ��");
         }
    }
   catch(IOException e1){System.out.println("����������");}
	}
	public void run()
	{
		try
		{
			in=new DataInputStream(user.getInputStream());
			out=new DataOutputStream(user.getOutputStream());
			user_name=in.readUTF();
			password=in.readUTF();
			if(user_name.equals("user")&&password.equals("123456")) //�û���Ϣ��֤
			{
				String command;        
				out.writeUTF("�û�user��¼�ɹ�");
				System.out.println("�û���"+user_name+" ������վ");
				while(!((command=in.readUTF()).equals("byebye")))
				{
					if(command.equals("dir"))                //�����ļ���Ϣ
					{          
						display();
						//out.writeUTF("���ӳɹ���");
					}
					else if(command.equals("load"))       //����ָ��
					{  
						String route=in.readUTF();
						File tem=new File(filepath+"\\"+route);
						if(tem.exists()&&tem.canRead())
						{
							out.writeUTF("ok");
							route=in.readUTF();
							if(route.equals("��ʼ����......"))
							{
								out.writeLong(tem.length());
								FileInputStream infile=new FileInputStream(tem);
								int n=0;
								byte b[]=new byte[1];
								while((n=infile.read(b))>0)
								{
									out.write(b,0,1);
								}               
								infile.close();
							}
							else if(route.equals("����ȡ��"))
								continue;
						}
						else
						{
							out.writeUTF("error");
						}             
					}
					else if(command.equals("build"))           //�ϴ��ļ�ָ��
					{    
						String route;
						route=in.readUTF();
						File newfile=new File(filepath+"\\"+route);
						if(newfile.createNewFile())
						{
							out.writeUTF("��ʼ�ϴ�......");
							long written_length=0,file_length=in.readLong();
							byte b[]=new byte[1];
							FileOutputStream mkfile=new FileOutputStream(newfile);
							int n=0;
							while((n=in.read(b))!=-1)
							{
								mkfile.write(b);
								written_length++;
								if(written_length>=file_length)break;
							}     
								mkfile.close();         
						}
						else
						{
							out.writeUTF("�ļ����Ѵ���!");
						}
            //newfile.close();
					}
					else if(command.equals("mkdir"))                //������Ŀ¼
					{    
						String route=in.readUTF();
						File mklist=new File(filepath+"\\"+route);
						if(!(mklist.exists()))
						{
							mklist.mkdir();
							out.writeUTF("��Ŀ¼�ѳɹ�����!");
							display();              
						}
						else
						{
							out.writeUTF("ָ�����ļ���ԭ���ļ����ظ����޷�������Ŀ¼!");
						}
					}
					else if(command.equals("delete"))              //ɾ��ָ���ļ�
					{    
						String messa=in.readUTF();
						File deletefile=new File(filepath+"\\"+messa);
						if(deletefile.isFile())
						{
							deletefile.delete();
							out.writeUTF("�ļ���ɾ��!");
							display();
						}
						else
						{
							out.writeUTF("�ļ�������");
						}
					}
					else if(command.equals("cd"))                      //����ָ��Ŀ¼
					{    
						display();
					}
					else if(command.equals("return"))                 //������һ��
					{    
						display();
					}
					else
					{
						out.writeUTF("error");
					}
				}
				user.close();
				System.out.println("�û���"+user_name+" ���뿪��վ!");
			}
			else 
			{
				out.writeUTF("�û���������������!");
				user.close();
			}			
				in.close();
				out.close();
				user.close();
			}
			catch(IOException e)
			{
				System.out.println(e);
			}
		}
	}
