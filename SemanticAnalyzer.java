import java.util.ArrayList;
import java.util.Stack;

public class SemanticAnalyzer {

    SemanticAnalyzer(){};
    ArrayList<MainTable> MT = new ArrayList<MainTable>();
    ArrayList<FunctionTable> FT = new ArrayList<FunctionTable>();
    public int scopenum = 0;
    Stack<Integer> currentscope = new Stack<Integer>();
    public static void main(String[] args) {
        // ArrayList<String> p1 = new ArrayList<>();
        // SemanticAnalyzer sa = new SemanticAnalyzer();
        // System.out.println(sa.insertMT("A", "Class", null, null));
        // System.out.println(sa.insertMT("B", "Class", "final", null));
        // System.out.println(sa.insertMT("B", "Class", "abstract", null));
    }

    boolean insertMT(String name, String type, String CM, ArrayList<String> parent)
    {
        // Check if duplicate entry
        if (lookupMT(name) != null) 
        {
            System.out.println("Duplicate");
            return false;
        }
        // Insert in the table if not duplicate
        else
        {
            MainTable m = new MainTable(name, type, CM, parent);
            MT.add(m);
            return true;
        }
    }

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

    boolean insertCT(String name, String type, String AM, boolean Fin, Boolean Static, String ClassName, boolean ABS)
    {
        ClassTable c = new ClassTable(name, type, AM, Fin, Static, ABS);
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


    FunctionTable lookupFT(String name,int scope)
    {
        for (int i = 0; i < FT.size(); i++) 
        {
            if (FT.get(i).name.equals(name) && FT.get(i).scope == scope) 
            {
                return FT.get(i);
            }
        }return null;
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

}

class MainTable
{
    String name = null;
    String type = null; // Class, ID, Function
    String CM = null; // Class Modifier
    ArrayList<String> parent;
    ArrayList<ClassTable> CT; // Class Table reference
    // int link = CT.hashCode();

    public MainTable(String name, String type, String CM, ArrayList<String> parent)
    {
        this.name = name; // Name of Function, Identifier, Class
        this.type = type; 
        this.CM = CM; // Class Modifiers (hide, const)
        this.parent = parent; // Inheritance for class
        this.CT = new ArrayList<ClassTable>();
    }
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
        this.name = name;
        this.type = type;
        this.AM = AM;
        this.Static = Static;
        this.Final = Final;
        this.ABS = ABS;
    }
}

class FunctionTable
{
    String name = null;
    String type = null;
    int scope = 0;

    public FunctionTable(String name, String type, int scope)
    {
        this.name = name;
        this.type = type;
        this.scope = scope;
    }
}
