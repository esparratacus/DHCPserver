/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dhcp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Clase que se encarga de cargar la configuración del servidor
 * @author Daniel Serrano Pardo
 */
public class DHCPDatabase {

    //Única instancia de DHCPDatabase
    private static DHCPDatabase _database = null;

    public static ArrayList<String> direcciones;
    public static String mascara;
    public static String DNS;
    public static String Gateway;
    public static int lease;
    public static int numDirecciones;
    
     public static ArrayList<String> direcciones1;
    public static String mascara1;
    public static String DNS1;
    public static String Gateway1;
    public static int lease1;
    public static int numDirecciones1;
    
    public static ArrayList<String> direcciones2;
    public static String mascara2;
    public static String DNS2;
    public static String Gateway2;
    public static int lease2;
    public static int numDirecciones2;
    
    public static ArrayList<Clientes> clientes = new ArrayList<Clientes>();

    public DHCPDatabase(){
        cargarConfiguracion();
    }

    public static DHCPDatabase getInstance(){
        if(_database==null)
            _database= new DHCPDatabase();
        return _database;
    }
    /**
     * Método que lee el archivo config.properties para cargar
     */

    public static void cargarConfiguracion(){
        Properties propiedades = new Properties();
        InputStream in = null;
        try{
            in = new FileInputStream("config.properties");
            propiedades.load(in);
            mascara= propiedades.getProperty("mascara");
            DNS=propiedades.getProperty("DNS");
            Gateway=propiedades.getProperty("Gateway");
            lease=Integer.parseInt(propiedades.getProperty("lease"));
            direcciones = new ArrayList<String> ();
            direcciones=obtenerDirecciones(propiedades.getProperty("IPinicial"),propiedades.getProperty("IPfinal"));
            numDirecciones = direcciones.size();

            mascara1= propiedades.getProperty("mascara1");
            DNS1=propiedades.getProperty("DNS1");
            Gateway1=propiedades.getProperty("Gateway1");
            lease1=Integer.parseInt(propiedades.getProperty("lease1"));
            direcciones1 = new ArrayList<String> ();
            direcciones1=obtenerDirecciones(propiedades.getProperty("IPinicial1"),propiedades.getProperty("IPfinal1"));
            numDirecciones1 = direcciones1.size();
            
            mascara2= propiedades.getProperty("mascara2");
            DNS2=propiedades.getProperty("DNS2");
            Gateway2=propiedades.getProperty("Gateway2");
            lease2=Integer.parseInt(propiedades.getProperty("lease2"));
            direcciones2 = new ArrayList<String> ();
            direcciones2=obtenerDirecciones(propiedades.getProperty("IPinicial2"),propiedades.getProperty("IPfinal2"));
            numDirecciones2 = direcciones2.size();
        }catch (IOException e){
            System.out.println("Error al cargar el archivo: " + e);
        }
    }

    /**
     * Método que crea un arreglo con todas las direcciones IP a ofrecer
     * @param inicio dirección inicial
     * @param termino dirección final
     * @return arreglo con las direcciones IP
     */

    private static ArrayList<String> obtenerDirecciones(String inicio,String termino){
        ArrayList<String> retorno = new ArrayList<String>();
        String temp = inicio;
        StringTokenizer st = new StringTokenizer(inicio, ".");
        int primero = Integer.parseInt(st.nextToken());
        int segundo = Integer.parseInt(st.nextToken());
        int tercero = Integer.parseInt(st.nextToken());
        int cuarto = Integer.parseInt(st.nextToken());
        while(!temp.equalsIgnoreCase(termino))
        {
            retorno.add(temp);
            cuarto++;
            if(cuarto==256)
            {
                cuarto=0;
                tercero++;
                if(tercero==256)
                {
                    tercero=0;
                    segundo++;
                    if(segundo==256)
                    {
                        segundo=0;
                        primero++;
                    }
                }
            }
            temp=String.valueOf(primero)+"."+String.valueOf(segundo)+"."+String.valueOf(tercero)+"."+String.valueOf(cuarto);
        }
        return retorno;
    }

    public static void imprimirDirecciones(){
        for(int i = 0 ; i<direcciones.size();i++)
            System.out.println(direcciones.get(i));
        for(int i = 0 ; i<direcciones1.size();i++)
            System.out.println(direcciones1.get(i));
        for(int i = 0 ; i<direcciones2.size();i++)
            System.out.println(direcciones2.get(i));
    }
    
    /**
     * Método que verifica si un cliente ya existe en la base de datos
     * @param MAC Mac del cliente buscado
     * @return true si existe, false si no existe
     */

    public boolean existeCliente(String MAC){
        for(int i=0; i <clientes.size();i++)
            if(MAC.equalsIgnoreCase(clientes.get(i).getIdCliente()))
                return true;
        return false;
    }
    
    /**
     * Método que retorna la dirección IP del cliente que está buscando
     * @param MAC del cliente
     * @return direccion del cliente
     */

    public String getIPdeMAC(String MAC){
        for(int i=0; i<clientes.size(); i++)
            if(MAC.equalsIgnoreCase(clientes.get(i).getIdCliente()))
                return clientes.get(i).getDirIP();
        return null;
    }

    /**
     * Método que retorna una IP que no esté en uso y la asocia a un cliente
     * @return dirección IP libre
     */

    public String getIPLibre(String MAC,String giaddr){
        
        ArrayList <String> dirs = identificarSubRed(giaddr);
        
        String mask = null, gate = null, dns = null;
        int lease = 0;
        
        if (dirs == null){
            System.out.println("El array List es nulo");
            return null;
        
        }
        
        if (dirs.equals(direcciones)){
            mask = mascara;
            gate = Gateway;
            lease = this.lease;
            dns = this.DNS;
        }
        
        if (dirs.equals(direcciones1)){
            mask = mascara1;
            gate = Gateway1;
            lease = this.lease1;
            dns = this.DNS1;
        }
        
        if (dirs.equals(direcciones2)){
            mask = mascara2;
            gate = Gateway2;
            lease = this.lease2;
            dns = this.DNS2;
        }
        if(!dirs.isEmpty())
        {
            Clientes nuevo = new Clientes(MAC, dirs.get(0));
            clientes.add(nuevo);
            nuevo.setDNS(dns);
            nuevo.setLease(lease);
            nuevo.setMask(mask);
            nuevo.setGateway(gate);
            return dirs.remove(0);
        }
        else{
            System.out.println("El array list esta vacio");
            return null;
            
        }
    }
    
    public ArrayList <String> identificarSubRed (String giaddr){
        if (giaddr.equals("0.0.0.0"))
            return direcciones;
        
        
        byte [] gate = getIPOfStr(giaddr);
        byte [] mask = getIPOfStr(mascara);
        byte [] mask1 = getIPOfStr(mascara1);
        byte [] mask2 = getIPOfStr(mascara2);
        
        byte [] red = getIPOfStr(Gateway);
        red [0] = (byte) (red [0] & mask [0]);
        red [1] = (byte) (red [1] & mask [1]);
        red [2] = (byte) (red [2] & mask [2]);
        red [3] = (byte) (red [3] & mask [3]);
        byte [] red1 = getIPOfStr(Gateway1);
        red1 [0] = (byte) (red1 [0] & mask1 [0]);
        red1 [1] = (byte) (red1 [1] & mask1 [1]);
        red1 [2] = (byte) (red1 [2] & mask1 [2]);
        red1 [3] = (byte) (red1 [3] & mask1 [3]);
        byte [] red2 = getIPOfStr(Gateway2);
        red2 [0] = (byte) (red2 [0] & mask2 [0]);
        red2 [1] = (byte) (red2 [1] & mask2 [1]);
        red2 [2] = (byte) (red2 [2] & mask2 [2]);
        red2 [3] = (byte) (red2 [3] & mask2 [3]);
        
        byte [] redT1 = new byte [4];
        redT1 [0] = (byte) (gate [0] & mask [0]);
        redT1 [1] = (byte) (gate [1] & mask [1]);
        redT1 [2] = (byte) (gate [2] & mask [2]);
        redT1 [3] = (byte) (gate [3] & mask [3]);
        
        byte [] redT2 = new byte [4];
        redT2 [0] = (byte) (gate [0] & mask1 [0]);
        redT2 [1] = (byte) (gate [1] & mask1 [1]);
        redT2 [2] = (byte) (gate [2] & mask1 [2]);
        redT2 [3] = (byte) (gate [3] & mask1 [3]);
        
        byte [] redT3 = new byte [4];
        redT3 [0] = (byte) (gate [0] & mask2 [0]);
        redT3 [1] = (byte) (gate [1] & mask2 [1]);
        redT3 [2] = (byte) (gate [2] & mask2 [2]);
        redT3 [3] = (byte) (gate [3] & mask2 [3]);
        
        if (red [0] == redT1 [0] && red [1] == redT1 [1] && red [2] == redT1 [2] && red [3] == redT1 [3]){
            return direcciones;
        }else if (red1 [0] == redT2 [0] && red1 [1] == redT2 [1] && red1 [2] == redT2 [2] && red1 [3] == redT2 [3]){
            return direcciones1;
        }
        else if (red2 [0] == redT3 [0] && red2 [1] == redT3 [1] && red2 [2] == redT3 [2] && red2 [3] == redT3 [3]){
            return direcciones2;
        }
        
        return null;
    }
    
    public byte[] getIPOfStr(String s){
        int j = 0;
        StringTokenizer ax = new StringTokenizer(s,".");
        byte[] b = new byte[4];
        b[0] = hexToByte(Integer.toHexString(Integer.parseInt(ax.nextToken())));
        b[1] = hexToByte(Integer.toHexString(Integer.parseInt(ax.nextToken())));
        b[2] = hexToByte(Integer.toHexString(Integer.parseInt(ax.nextToken())));
        b[3] = hexToByte(Integer.toHexString(Integer.parseInt(ax.nextToken())));
        return b;
    }
    
    private byte hexToByte(String s){
        return (byte) Integer.parseInt(s, 16);
        }
    
    public String eliminarCliente(String MAC, String giaddr){
        String retorno = null;
        for(int i=0; i<clientes.size(); i++)
            if(MAC.equalsIgnoreCase(clientes.get(i).getIdCliente()))
            {
                if (!clientes.get(i).getForzado()){
                    ArrayList <String> dirs = identificarSubRed(giaddr);
                    
                    dirs.add(clientes.get(i).getDirIP());
                }
                retorno = clientes.get(i).getDirIP();
                clientes.remove(i);
            }
        return retorno;
    }

    public Clientes getCliente(String MAC){
        for(Clientes temp: clientes)
            if(MAC.equalsIgnoreCase(temp.getIdCliente()))
                return temp;
        return null;
    }

    public int getIndice(String MAC){
        for(int i=0; i<clientes.size(); i++)
            if(MAC.equalsIgnoreCase(clientes.get(i).getIdCliente()))
                return i;
        return -1;
    }
    
    public int buscarIp (String ip){
        for (int i=0; i<direcciones.size();i++){
            if (direcciones.get(i).equals(ip))
                return i;
        }
        for (int i=0; i<direcciones1.size();i++){
            if (direcciones1.get(i).equals(ip))
                return i;
        }
        for (int i=0; i<direcciones2.size();i++){
            if (direcciones2.get(i).equals(ip))
                return i;
        }
        return -1;
    }
    
    public String sacarIP (int indice, String MAC, String giaddr){
        ArrayList <String> dirs = identificarSubRed(giaddr);
        Clientes nuevo = new Clientes(MAC, dirs.get(indice));
        clientes.add(nuevo);
        return dirs.remove(indice);
    }
    
    public void liberarIP (String MAC, String giaddr){
        int i =0;
        for (i = 0; i < clientes.size(); i++) {
            if(MAC.equalsIgnoreCase(clientes.get(i).getIdCliente())){
                Clientes c = clientes.remove(i);
                if(!c.getForzado()){
                    ArrayList <String> dirs = identificarSubRed(giaddr);
                    dirs.add(c.getDirIP());
                }
                break;
            }
        }
        
    }
    
    public void elminiarClienteDecline (String MAC){
        for (int i = 0; i < clientes.size(); i++) {
            if(MAC.equalsIgnoreCase(clientes.get(i).getIdCliente())){
                clientes.remove(i);
                break;
            }
        }
    }
    
    public void agregarClienteForzado (String MAC, String IP){
        Clientes nuevo = new Clientes(MAC, IP);
        nuevo.setForzado(true);
        clientes.add(nuevo);
    }

}
