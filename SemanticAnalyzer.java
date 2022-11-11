import java.util.ArrayList;
import java.util.Stack;

public class SemanticAnalyzer {

    SemanticAnalyzer(){};
    ArrayList<MainTable> MT = new ArrayList<>();
    ArrayList<FunctionTable> FT = new ArrayList<>(); // Function table
    ArrayList<LocalTable> LT = new ArrayList<>(); // Local table (if, for, while)
    public int scopenum = 0;
    Stack<Integer> currentscope = new Stack<Integer>();
    public static void main(String[] args) {
        // ArrayList<String> p1 = new ArrayList<>();
        // SemanticAnalyzer sa = new SemanticAnalyzer();
        // System.out.println(sa.insertMT("A", "Class", null, null));
        // System.out.println(sa.insertMT("B", "Class", "final", null));
        // System.out.println(sa.insertMT("B", "Class", "abstract", null));
    }

    boolean insertMT(String name, String type, String DT, String CM, ArrayList<String> parent, boolean isClass)
    {
        // Insertion for Class or Variable
        if ( isClass )
        {
            // Check if duplicate entry
            if (lookupMT(name) != null) 
            {
                // System.out.println("Duplicate");
                return false;
            }
            // Insert in the table if not duplicate
            else
            {
                MainTable m = new MainTable(name, type, DT, CM, parent, isClass);
                MT.add(m);
                return true;
            }
        }
        // Insertion for Function
        else
        {
            if ( LookupMTf(name, DT) != null )
            {
                // System.out.println("Duplicate Function Error");
                return false;
            }
            else
            {
                MainTable m = new MainTable(name, type, DT, CM, parent, isClass);
                MT.add(m);
                return true;
            }
        }
    }

    // Lookup for class and variables
    MainTable lookupMT(String name)
    {
        for (int i = 0; i < MT.size(); i++) 
        {
            if (MT.get(i).name.equals(name)) 
            {
                return MT.get(i);
            }
        }
        return null;
    }

    // Lookup for function
    MainTable LookupMTf(String Name, String PL)
    {
        for (int i = 0; i < MT.size(); i++) 
        {
            MainTable m = MT.get(i);
            if (m.name.equals(Name)) // Name matches
            {
                if ( m.DT.equals(PL) ) // Param list matches
                {
                    return m;
                }
            }
        }
        return null;
    }

    boolean insertCT(ClassTable c, String ClassName)
    {
        // ClassTable c = new ClassTable(name, type, AM, Fin, Static, ABS);
        for (int i = 0; i < MT.size(); i++) 
        {
            MainTable m = MT.get(i);
            if (m.name.equals(ClassName)) 
            {
                m.CT.add(c);
                return true;
            }
        }return false;
    }

    // Class Lookup for Attributes
    ClassTable LookupCTa(String Name, String ClassName)
    {
        for (int i = 0; i < MT.size(); i++) 
        {
            MainTable m = MT.get(i);
            if (m.name.equals(ClassName)) 
            {
                for (int j = 0; j < m.CT.size(); j++) 
                {
                    ClassTable c = m.CT.get(j);
                    if (c.name.equals(Name)) 
                    {
                        return c;
                    }
                }
            }
        }
        return null;
    }

    // Class Lookup for Functions
    ClassTable LookupCTf(String Name, String PL, String ClassName)
    {
        for (int i = 0; i < MT.size(); i++) 
        {
            MainTable m = MT.get(i);
            if (m.name.equals(ClassName)) 
            {
                for (int j = 0; j < m.CT.size(); j++) 
                {
                    ClassTable c = m.CT.get(j);
                    if (c.name.equals(Name)) 
                    {
                        if (c.type.equals(PL)) 
                        {
                            return c;
                        }
                    }
                }
            }
        }
        return null;
    }

    boolean insertFT(String name, String type, int scope)
    {
    
            for (FunctionTable item : FT) {
                if (item.name.equals(name) && item.scope == scope) {
                    return false;
                }
            }
    
            FT.add(new FunctionTable(name, type, scope));
            System.out.println(name + " " + type + " " + scope);
            return true;
    }


    String lookupFT(String name,int scope)
    {
        for (int i = 0; i < FT.size(); i++) 
        {
            if (FT.get(i).name.equals(name) && FT.get(i).scope == scope) 
            {
                return FT.get(i).type;
            }
        }
        return null;
    }

    boolean insertLT(String name, String type, int scope)
    {
    
            for (LocalTable item : LT) {
                if (item.name.equals(name) && item.scope == scope) {
                    return false;
                }
            }
    
            LT.add(new LocalTable(name, type, scope));
            System.out.println(name + " " + type + " " + scope);
            return true;
    }


    String lookupLT(String name,int scope)
    {
        for (int i = 0; i < LT.size(); i++) 
        {
            if (LT.get(i).name.equals(name) && LT.get(i).scope == scope) 
            {
                return LT.get(i).type;
            }
        }
        return null;
    }
    
    public void createScope()
    {
        scopenum++;
        currentscope.push(scopenum);
    }

    public void destroyScope()
    {
        currentscope.pop();
    }

    // Compatibility check
    /*  Cases: 
    1. int + int = int
    2. int + float = float
    3. float + int = float
    4. float + float = float
    */
    public String compatibilityCheck(String leftType, String rightType, String operator) 
    {
        if (operator.equals("+") || operator.equals("-") || operator.equals("*") || operator.equals("/") || operator.equals("%")) {
            
            // String compatibility
            if ( operator.equals("+") && leftType.equals("str") && rightType.equals("str") )
            {
                String type = "str";
                return type;
            }

            // case 1
            if (leftType.equals("int") && rightType.equals("int")) 
            {
                String type = "int";
                return type;
            }

            else if (leftType.equals("int") && rightType.equals("float") || 
                    leftType.equals("float") && rightType.equals("int") ||
                    leftType.equals("float") && rightType.equals("float") )
            {
                String type = "float";
                return type;            
            }
        }
        return null;
    }

}

class MainTable
{
    String name = null;
    String type = null; //Entity type: Class, IDF, Function
    String DT = null; // DT for global variables
    String CM = null; // Class Modifier
    ArrayList<String> parent;
    ArrayList<ClassTable> CT; // Class Table reference
    // int link = CT.hashCode();

    // For class
    public MainTable(String name, String type, String DT, String CM, ArrayList<String> parent, boolean isClass)
    {
        this.name = name; // Name of Function, Identifier, Class
        this.type = type; 
        this.DT = DT;
        this.CM = CM; // Class Modifiers (hide, const)
        this.parent = parent; // Inheritance for class
        // Classtable will be created for classes but not for Global variables or functions
        if (  isClass ){
            this.CT = new ArrayList<ClassTable>();
        }
    }

    // For Variables

    // For function


}

class ClassTable
{
    String name = null;
    String type = null; // Data type and functions paramters type
    String AM = null;
    boolean Final = false;
    boolean Static = false; // Static
    boolean ABS = false; // Abstract

    public ClassTable(String name, String type, String AM, boolean Final, Boolean Static, boolean ABS)
    {
        this.name = name; // Name of attribute or method
        this.type = type; // DT of attribute or Method parameter list DT
        this.AM = AM;
        this.Static = Static;
        this.Final = Final;
        this.ABS = ABS;
    }
}

class FunctionTable
{
    String name = null; // variable name
    String type = null; // DT
    int scope = 0;

    public FunctionTable(String name, String type, int scope)
    {
        this.name = name;
        this.type = type;
        this.scope = scope;
    }
}

class LocalTable
{
    String name = null; // variable name
    String type = null; // DT
    int scope = 0;

    public LocalTable(String name, String type, int scope)
    {
        this.name = name;
        this.type = type;
        this.scope = scope;
    }
}
