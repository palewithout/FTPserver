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
	  System.out.println("*******************服务器已关闭*******************");
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
    System.out.println("*******************服务器已启动*******************");    
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
            	   out.writeUTF("文件夹");
            	   out.writeUTF("  ");
            	   }
               else {
            	   out.writeUTF("文件");
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
           out.writeUTF("路径不正确！");
         }
    }
   catch(IOException e1){System.out.println("网络阻塞！");}
	}
	public void run()
	{
		try
		{
			in=new DataInputStream(user.getInputStream());
			out=new DataOutputStream(user.getOutputStream());
			user_name=in.readUTF();
			password=in.readUTF();
			if(user_name.equals("user")&&password.equals("123456")) //用户信息验证
			{
				String command;        
				out.writeUTF("用户user登录成功");
				System.out.println("用户："+user_name+" 来到本站");
				while(!((command=in.readUTF()).equals("byebye")))
				{
					if(command.equals("dir"))                //返回文件信息
					{          
						display();
						//out.writeUTF("连接成功！");
					}
					else if(command.equals("load"))       //下载指令
					{  
						String route=in.readUTF();
						File tem=new File(filepath+"\\"+route);
						if(tem.exists()&&tem.canRead())
						{
							out.writeUTF("ok");
							route=in.readUTF();
							if(route.equals("开始接收......"))
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
							else if(route.equals("操作取消"))
								continue;
						}
						else
						{
							out.writeUTF("error");
						}             
					}
					else if(command.equals("build"))           //上传文件指令
					{    
						String route;
						route=in.readUTF();
						File newfile=new File(filepath+"\\"+route);
						if(newfile.createNewFile())
						{
							out.writeUTF("开始上传......");
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
							out.writeUTF("文件名已存在!");
						}
            //newfile.close();
					}
					else if(command.equals("mkdir"))                //创建新目录
					{    
						String route=in.readUTF();
						File mklist=new File(filepath+"\\"+route);
						if(!(mklist.exists()))
						{
							mklist.mkdir();
							out.writeUTF("新目录已成功创建!");
							display();              
						}
						else
						{
							out.writeUTF("指定的文件与原有文件名重复，无法创建该目录!");
						}
					}
					else if(command.equals("delete"))              //删除指定文件
					{    
						String messa=in.readUTF();
						File deletefile=new File(filepath+"\\"+messa);
						if(deletefile.isFile())
						{
							deletefile.delete();
							out.writeUTF("文件已删除!");
							display();
						}
						else
						{
							out.writeUTF("文件不存在");
						}
					}
					else if(command.equals("cd"))                      //进入指定目录
					{    
						display();
					}
					else if(command.equals("return"))                 //返回上一级
					{    
						display();
					}
					else
					{
						out.writeUTF("error");
					}
				}
				user.close();
				System.out.println("用户："+user_name+" 已离开本站!");
			}
			else 
			{
				out.writeUTF("用户名或口令输入错误!");
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
