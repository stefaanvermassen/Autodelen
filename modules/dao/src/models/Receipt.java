    package models;
    import org.joda.time.DateTime;

    import java.util.*;
    import  java.io.*;
    import  org.apache.poi.*;
    import  org.apache.poi.hssf.usermodel.HSSFSheet;
    import  org.apache.poi.hssf.usermodel.HSSFWorkbook;
    import  org.apache.poi.hssf.usermodel.HSSFRow;
    import  org.apache.poi.hssf.usermodel.HSSFCell;

public class Receipt {

    private int id;
    private String name;
    private File files;
    private DateTime date;
    private User user;

    public Receipt(int id, String name, File files, DateTime date, User user) {
        this.id = id;
        this.name = name;
        this.files = files;
        this.date = date;
        this.user=user;
    }

    public Receipt(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFiles() {
        return files;
    }

    public void setFiles(File files) {
        this.files = files;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static Receipt generate(int id, String name, File files, DateTime date, User user){
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("rapport");
        //HSSFSheet sheet = workbook.createSheet("Rapport voor "+user.getId()+" "+date.toYearMonthDay().toString());
        
	//Hoofdding
	
	//Create a new row in current sheet
        HSSFRow row = sheet.createRow(0);
        //Create a new cell in current row
        HSSFCell cell = row.createCell(0);
        //Set value to new value
        cell.setCellValue(
		"Rapport voor "
		+user.getFirstName()+" "
		+user.getLastName()+" voor "
		+date.toYearMonthDay().toString());
 
Map<String, Object[]> data = new HashMap<String, Object[]>();
data.put("1", new Object[] {"Emp No.", "Name", "Salary"});
data.put("2", new Object[] {1d, "John", 1500000d});
data.put("3", new Object[] {2d, "Sam", 800000d});
data.put("4", new Object[] {3d, "Dean", 700000d});
 
        Set<String> keyset = data.keySet();
        int rownum = 0;
        for (String key : keyset) {
            row = sheet.createRow(rownum++);
            Object [] objArr = data.get(key);
            int cellnum = 0;
            for (Object obj : objArr) {
                cell = row.createCell(cellnum++);
                if(obj instanceof Date)
                    cell.setCellValue((Date)obj);
                else if(obj instanceof Boolean)
                    cell.setCellValue((Boolean)obj);
                else if(obj instanceof String)
                    cell.setCellValue((String)obj);
                else if(obj instanceof Double)
                    cell.setCellValue((Double)obj);
            }
        }

        //Save the file
        try {
	    java.io.File file=new java.io.File("/Rapporten/rapport_"+user.getId()+"_"+date.toYearMonthDay().toString()+".xls");
	    file.createNewFile();
            FileOutputStream out =new FileOutputStream(file);
            workbook.write(out);
            out.close();
            System.out.println("Excel written successfully..");
     
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Create a new file instance
	//Create a new Receipt instance
	return new Receipt(id, name, files, date, user);
    }
}

