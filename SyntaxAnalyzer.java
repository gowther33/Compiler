// Every rule has selection set
import java.util.ArrayList;
// Error in Dec Init List
import java.util.function.Function;

public class SyntaxAnalyzer{

    private int index = 0;
    public ArrayList<Token> tokens;
    public boolean Semantic = false;

    // Semantic Identifiers
    String type = null; // Entities: Class, IDF, Function
    String name = null; // Name for the entity
    String CM = null; // Clas Modifier ( hide, const)
    String AM = null; // Access Modifier for Attributes and class methods
    String DT = null; // Data type for variables
    String Classname = null; // Classname
    boolean Final = false;
    boolean Static = false;
    boolean ABS = false;
    ArrayList<String> parent;

    int index2 = 0; // This index keeps track of Main table entries
    boolean Class = false; // This flag keeps track of whether insertion in MT is for OOP or General
    
    // Whenever there is local entry global = false
    // After insertion global = true
    boolean global = true; // This keeps track of global and local declarations
    boolean function = false; // This flag is for insertion in function table


    // Semantic Object
    SemanticAnalyzer SMA = new SemanticAnalyzer();

    public static void main(String[] args) {
        LexicalAnalyzer LA = new LexicalAnalyzer();
        LA.generateTokens();
        ArrayList<Token> T  = LA.getTokens();
        SyntaxAnalyzer SA = new SyntaxAnalyzer(T);
        System.out.println(SA.start());
    }

    // The object will be made in lexical analyzer after tokens has been made
    public SyntaxAnalyzer(ArrayList<Token> tokens){
        this.tokens = tokens;
    }

    // Start
    public boolean start(){
        if ( tokens.get(index).CP.equals("DT") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("if") || tokens.get(index).CP.equals("for") ||
        tokens.get(index).CP.equals("while" ) || tokens.get(index).CP.equals("func" ) || tokens.get(index).CP.equals("try") || tokens.get(index).CP.equals("return") ||
        tokens.get(index).CP.equals("break") || tokens.get(index).CP.equals("continue") || tokens.get(index).CP.equals("IncDec") || tokens.get(index).CP.equals("my") ||
        tokens.get(index).CP.equals("base")
        )
        {
            if (SST())
                {
                    if (start()) {
                        return true;
                    }
                }
        }    
            
        else if ( tokens.get(index).CP.equals("class") || tokens.get(index).CP.equals("hide") || tokens.get(index).CP.equals("final") )
        {
            if(Class())
                {
                    if (start()){
                        return true;
                    }
                }
        }
        // EndMarker
        else if ( tokens.get(index).CP.equals("?")){
            return true;
        }
        // Logic for differentiating syntax and semantic errors
        System.out.println("Syntax Error at line "+tokens.get(index).lineNo);
        return false;
    }

    // -------------------------- OOP -----------------------------------------------------
    // Class
    public boolean Class(){
        if ( tokens.get(index).CP.equals("class") || tokens.get(index).CP.equals("hide") || tokens.get(index).CP.equals("final") )
        {   // Synthesized Attribute
            // Class Modifier: hide, const
            if (C_Type(CM)){
                if (tokens.get(index).CP.equals("class")){
                    // Reading terminal increments index
                    type = "Class";
                    index++;
                    if (tokens.get(index).CP.equals("ID")){
                        // Name of class from value part
                        name = tokens.get(index).value;
                        Classname = name; // To keep track of classname
                        // Reading terminal increments index
                        index++;
                        // The ID parent will be initialized with a new object reference everytime this method is called
                        parent = new ArrayList<>();
                        if (Inherit(parent)){
                            // Enter into main table
                            Class = true; // Insertion is for Class
                            if ( ! SMA.insertMT(name, type, CM, parent, Class) )
                            {
                                System.out.println("Duplicate Class "+Classname+" Error!");
                                Semantic = true;
                                return false;
                            }
                            // Restore values after insertion
                            name = null;
                            type = null;
                            CM = null;
                            parent = null;
                            Class = false; 
                            if (tokens.get(index).CP.equals("lcb")){
                                // Reading terminal increments index
                                SMA.createScope();
                                index++;
                                // Class body will inherit Class table reference
                                if (C_Body()){
                                    // Index2 is incremented after each entry in the main table
                                    // index2++;
                                    if (tokens.get(index).CP.equals("rcb")){
                                        // Reading terminal increments index
                                        SMA.destroyScope();
                                        index++;
                                        return true;
                                    }
                                }

                            }
                        }
                    }

                }
            }
        }
        return false;
    }

    // Class Types
    public boolean C_Type(String CM){
        // Rule 1
        if ( tokens.get(index).CP.equals("hide"))
        {
            // Put value part in the attribute
            CM = tokens.get(index).value;
            index++;
            return true;
        }

        // Rule 2
        else if ( tokens.get(index).CP.equals("const") ) 
        {
            // Put value part in the attribute
            CM = tokens.get(index).value;
            index++;
            return true;
        }

        // Null
        else if  ( tokens.get(index).CP.equals("class") ){
            return true;
        }

        return false;
    }

    // Inheritance
    public boolean Inherit(ArrayList<String> parent){
        // Rule 1
        if ( tokens.get(index).CP.equals("INH") )
        {
            index++;
            if (tokens.get(index).CP.equals("ID"))
            {
                name = tokens.get(index).value;
                index++;
                // Lookup in main table if the inherited class exists
                MainTable T = SMA.lookupMT(name); // returns maintable object
                // If Identifier
                if ( T.type.equals("IDF") )
                {
                    System.out.println("Cannot inherit from an identifier!");
                    Semantic = true;
                    return false;
                }
                // If function
                else if ( T.type.equals("Function") )
                {
                    System.out.println("Cannot inherit from a Function!");
                    Semantic = true;
                    return false;
                }
                // If final class
                else if ( T.type.equals("Class") && T.CM.equals("const"))
                {
                    System.out.println("Final class cannot be inherited!");
                    Semantic = true;
                    return false;
                }
                parent.add(name);
                // If multiple inheritance
                if (ILIST(parent))
                {
                    return true;
                }
            }
        }
        // Null case {
        else if ( tokens.get(index).CP.equals("lcb") ){
            return true;
        }
        return false;
    }

    // Ilist
    public boolean ILIST(ArrayList<String> parent){
        // Rule 1
        if ( tokens.get(index).CP.equals("comma") )
        {
            index++;
            if ( tokens.get(index).CP.equals("ID") )
            {
                name = tokens.get(index).value;
                index++;
                // Lookup in main table if the inherited class exists
                MainTable T = SMA.lookupMT(name); // returns maintable object
                // If Identifier
                if ( T.type.equals("IDF") )
                {
                    System.out.println("Cannot inherit from an identifier!");
                    Semantic = true;
                    return false;
                }
                // If function
                else if ( T.type.equals("Function") )
                {
                    System.out.println("Cannot inherit from a Function!");
                    Semantic = true;
                    return false;
                }
                // If final class
                else if ( T.type.equals("Class") && T.CM.equals("const"))
                {
                    System.out.println("Final class cannot be inherited!");
                    Semantic = true;
                    return false;
                }
                parent.add(name);
                // If more inheritance
                if (ILIST(parent)) 
                {
                    return true;
                }
            }
        }
        // Null {
        else if ( tokens.get(index).CP.equals("lcb") ){
            return true;
        }
        return false;
    }

    // Class Body
    public boolean C_Body(){
        // Rule 1
        if ( tokens.get(index).CP.equals("func") ){
            if (C_Func()){
                if ( C_Body() ) {
                    return true;
                }
            }
        }
        // Rule 2
        else if ( tokens.get(index).CP.equals("AM") ){
            if (ATR()){
                if ( C_Body() ){
                    return true;
                }
            }
        }
        // Null }
        else if ( tokens.get(index).CP.equals("rcb") ) {
            return true;
        }
        return false;
    }
    
    // Class Function
    public boolean C_Func(){
        if ( tokens.get(index).CP.equals("func") ) 
        {
            // Terminal
            index++;
            if ( AM() )
            {
                if ( FDEF() )
                {
                    return true;
                }
            }
        }
        return false;
    }

    // Access Modifiers
    // AM is neccessary in our syntax
    // $ = public
    // # = private
    // ## = protected
    public boolean AM(){
        if ( tokens.get(index).CP.equals("AM") ) 
        {
            AM = tokens.get(index).value;
            // If public
            if ( AM.equals("$") )
            {
                AM = "Public";
            }
            // Private
            else if ( AM.equals("#") )
            {
                AM = "Private";
            }
            // Protected
            else if ( AM.equals("##") )
            {
                AM = "Protected";
            }
            index++;
            return true;
        }
        return false;
    }

    // Class Function Definition
    public boolean FDEF(){
        // Rule 1
        if ( tokens.get(index).CP.equals("static") || tokens.get(index).CP.equals("final") || tokens.get(index).CP.equals("ID") )
        {
            if ( F_Type() ) 
            {
                if ( More() )
                {
                    if ( Func_Body() )
                    {
                        return true;
                    }
                }
            }
        }

        // Rule 2
        else if ( tokens.get(index).CP.equals("abstract") )
        {
            ABS = true;
            index++;
            if ( More() )
            {
                if ( tokens.get(index).CP.equals("semicolon") )
                {
                    index++;
                    return true;
                }
            }
        }
        return false; 
    }

    // Function types
    public boolean F_Type(){
        // Rule 1
        if ( tokens.get(index).CP.equals("static") || tokens.get(index).CP.equals("final") )
        {
            if( T() ){
                return true;
            }
        }
        // Null
        else if ( tokens.get(index).CP.equals("ID") )
        {
            return true;
        }
        return false;
    }

    // Method and Field type
    public boolean T() {
        // Rule 1
        if (tokens.get(index).CP.equals("static")){
            Static = true;
            index++;
            if ( T1() ){
                return true;
            }
        }
        // Rule 2
        else if (tokens.get(index).CP.equals("const")){
            Final = true;
            index++;
            if ( T2() ){
                return true;
            }
        }
        return false;
    }

    // T1
    public boolean T1(){
        // Rule 1
        if (tokens.get(index).CP.equals("const"))
        {
            Final = true;
            index++;
            return true;
        }

        // Null 
        else if ( tokens.get(index).CP.equals("rcb") ){
            return true;
        }
        return false;
    }

    // T2
    public boolean T2(){
        // Rule 1
        if (tokens.get(index).CP.equals("static"))
        {
            Static = true;
            index++;
            return true;
        }

        // Null
        else if ( tokens.get(index).CP.equals("rcb") ){
            return true;
        }
        return false;
    }


    // More
    // Insertion in Class table for function
    public boolean More(){
        if ( tokens.get(index).CP.equals("ID") )
        {
            // Name of function
            name = tokens.get(index).value;
            index++;
            if ( tokens.get(index).CP.equals("lp") ) 
            {
                SMA.createScope(); // Class Function scope starts
                String params = ""; // To store parametes' datatype, separated by ,
                index++;
                if ( Param(params) )
                {
                    // Add to Classtable
                    // Look if duplicate entry
                    if ( SMA.LookupCTf(name, params, Classname) == null )
                    {
                        ClassTable C = new ClassTable(name, params, AM, Final, Static, ABS);
                        SMA.insertCT(C, Classname);
                        name = null;
                        AM = null;
                        Final = false;
                        Static = false;
                        ABS = false;
                    }
                    else{
                        // Duplication Error
                        System.out.println("Duplicate Function "+ name);
                        Semantic = true;
                        return false;
                    }
                        
                    if ( tokens.get(index).CP.equals("rp") )
                    {
                        index++;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Function body Both for OOP and Functional
    public boolean Func_Body(){
        if ( tokens.get(index).CP.equals("lcb") )
        {
            index++;
            if ( B() )
            {
                SMA.destroyScope(); // Class Function scope closes
                return true;
            }
        }   
        return false;
    }

    // B
    public boolean B(){
        if ( tokens.get(index).CP.equals("rcb") ) 
        {
            index++;
             return true;
        }

        else if ( tokens.get(index).CP.equals("semicolon") )
        {
            index++;
            if ( B2() )
            {
                if ( tokens.get(index).CP.equals("rcb") )
                {
                    index++;
                    return true;
                }
            }
        }
        // MST }
        else if ( tokens.get(index).CP.equals("DT") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("if") || tokens.get(index).CP.equals("for") ||
        tokens.get(index).CP.equals("while") || tokens.get(index).CP.equals("func" ) || tokens.get(index).CP.equals("try") || tokens.get(index).CP.equals("return") ||
        tokens.get(index).CP.equals("break") || tokens.get(index).CP.equals("continue") || tokens.get(index).CP.equals("IncDec") || tokens.get(index).CP.equals("my") ||
        tokens.get(index).CP.equals("base") )
        {
            if ( MST() )
            {
                if ( tokens.get(index).CP.equals("rcb") )
                {
                    index++;
                    return true;
                }
            }
        }

        return false;
    }
    
    // Attributes
    public boolean ATR(){
        if ( tokens.get(index).CP.equals("AM") )
        {
            if ( AM() ) // AM receives value of AM
            {
                if ( Type() ){ // type receives value of static or const
                    if ( A() ){ // receives datatype and name
                        // Insertion in classtable is handled in DEC
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Atttributes type
    public boolean Type()
    {
        if ( tokens.get(index).CP.equals("static") || tokens.get(index).CP.equals("const") )
        {
            if ( T() )
            {
                return true;
            }
        }

        // Null 
        else if ( tokens.get(index).CP.equals("DT") )
        {
            return true;
        }
        return false;
    }

    // A
    public boolean A(){
        if ( tokens.get(index).CP.equals("DT") )
        {
            Class = true;
            if ( DEC() ){
                return true;
            }
        }
        return false;
    }

    // -------------------------- OOP -----------------------------------------------------

    // -------------------------------------------------------- This & Super  
    public boolean This()
    {
        if ( tokens.get(index).CP.equals("my") )
        {
            index++;
            if ( C() )
            {
                return true;
            }
        }
        return false;
    }

    // Super
    public boolean Super()
    {
        if ( tokens.get(index).CP.equals("base") )
        {
            index++;
            if ( C() )
            {
                return true;
            }
        }
        return false;       
    }

    // C
    public boolean C()
    {
        if (tokens.get(index).CP.equals("ref" ) )
        {
            index++;
            if ( R() )
            {
                return true;
            }
        }

        else {
            if ( tokens.get(index).CP.equals("lp" ) )
            {
                index++;
                if ( ARGS() )
                {
                    if ( tokens.get(index).CP.equals("rp") )
                    {
                        index++;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // -------------------------------------------------------- This & Super  

    // ------------------------------------ OOP & Functional -------------------------------------------------
    // Declaration
    // After declaration can come SST 
    // FOllow of Dec and All will contain SST
    public boolean DEC(){
        // Terminal
        if ( tokens.get(index).CP.equals("DT") ){
            DT = tokens.get(index).value; // DT of variable
            index++;
            if ( XO() ){
                return true;
            }
        }
        return false;
    }

    // XO
    public boolean XO(){
        // Rule 1
        if ( tokens.get(index).CP.equals("ID") ){
            name = tokens.get(index).value; // Name of variable
            index++;
            // Insert in Classtable if Class flag is true else insert in Maintable
            if (Class == true)
            {
                // Look if duplicate entry
                if ( SMA.LookupCTa(name, Classname) == null )
                {
                    ClassTable C = new ClassTable(name, DT, AM, Final, Static, ABS);
                    SMA.insertCT(C, Classname);
                    name = null;
                    DT = null;
                    AM = null;
                    Final = false;
                    Static = false;
                }
                else{
                    // Duplication Error
                    System.out.println("Duplicate Identifier "+ name);
                    Semantic = true; // Semantic error
                    return false;
                }
            }
            // Incase of Global declaration
            if (Class == false && global) 
            {
                // Enter into Maintable
                if ( ! SMA.insertMT(name, type, DT, CM, parent, Class) )
                {
                    // Duplication Error
                    System.out.println("Duplicate Global Variable "+ name);
                    Semantic = true;
                    return false;
                }
            }
            if ( NEW() ){
                return true;
            }
        }
        // Rule 2
        else if ( tokens.get(index).CP.equals("[") )
        {
            if ( ARRAY() ){
                if ( Z() ){
                    return true;
                }
            }
        }

        return false;
    }
    // NEW
    public boolean NEW()
    {
        // Rule 1
        if ( tokens.get(index).CP.equals("AsgnOp") )
        {
            if ( INIT() )
            {
                if ( LIST() )
                {
                    return true;
                }
            }
        }
        // Rule 2
        else if ( tokens.get(index).CP.equals("comma") || tokens.get(index).CP.equals("semicolon") )
        {
            if ( LIST() )
            {
                return true;
            }
        }
        return false;
    }


    // INIT
    public boolean INIT(){
        if ( tokens.get(index).CP.equals("AsgnOp") ){
            index++;
            if ( NEW2() ){
                return true;
            }
        }

        // Null rule
        if ( tokens.get(index).CP.equals("comma") || tokens.get(index).CP.equals("semicolon") )
        {
            return true;
        }
        return false;
    }

    // LIST
    public boolean LIST(){
        if ( tokens.get(index).CP.equals("semicolon") ){
            index++;
            return true;
        }

        else if ( tokens.get(index).CP.equals("comma") ){
            index++;
            if ( tokens.get(index).CP.equals("ID") ){
                // For multiple declaration
                name = tokens.get(index).value;
                index++;
                // Insert in Classtable if flag is true else insert in Maintable
                if (Class)
                {
                    // Look if duplicate entry
                    if ( SMA.LookupCTa(name, Classname) != null )
                    {
                        ClassTable C = new ClassTable(name, DT, AM, Final, Static, ABS);
                        SMA.insertCT(C, Classname);
                    }
                }
                
                if ( NEW() ){
                    return true;
                }
            }
        }
        return false;
    }

    // NEW 2
    public boolean NEW2()
    {
        // Rule 1 <OE>
        if ( tokens.get(index).CP.equals("int") || tokens.get(index).CP.equals("float") || tokens.get(index).CP.equals("str") || tokens.get(index).CP.equals("char") ||
            tokens.get(index).CP.equals("bool") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("lp") || tokens.get(index).CP.equals("LogNot")
            )
        {
            if ( OE() )
            {
                return true;
            }
        }

        // Rule 2
        else if ( tokens.get(index).CP.equals("new") )
        {
            index++;
            if ( tokens.get(index).CP.equals("DT")  )
            {
                index++;
                if ( NT1() )
                {
                    return true;
                }
            }
        }
        return false;
    }

    // Z
    public boolean Z(){
        if ( tokens.get(index).CP.equals("ID") )
        {
            index++;
            if ( NEW() )
            {   
                return true;
            }
        }
        return false;
    }


    // Array
    public boolean ARRAY(){
        if ( tokens.get(index).CP.equals("[") ){
            index++;
            if ( NT() ){
                return true;
            }
        }
        return false;
    }

    // NT
    public boolean NT(){
        if ( tokens.get(index).CP.equals("]") ){
            index++;
            if ( ND() ){
                return true;
            }
        }
        return false;
    }

    // ND
    public boolean ND(){
        if ( tokens.get(index).CP.equals("[") ){
            index++;
            if ( tokens.get(index).CP.equals("]") ){
                index++;
                if ( ND() ){
                    return true;
                }
            }
        }

        // Null After array can come another SST 
        // Need to check
        else if ( tokens.get(index).CP.equals("AsgnOp") ||  tokens.get(index).CP.equals("semicolon") || tokens.get(index).CP.equals("comma") ||
                tokens.get(index).CP.equals("DT") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("if") || 
                tokens.get(index).CP.equals("for") || tokens.get(index).CP.equals("while") || tokens.get(index).CP.equals("func" ) || 
                tokens.get(index).CP.equals("try") || tokens.get(index).CP.equals("return") || tokens.get(index).CP.equals("break") || 
                tokens.get(index).CP.equals("continue") || tokens.get(index).CP.equals("IncDec") || tokens.get(index).CP.equals("my") || tokens.get(index).CP.equals("base")
            ) 
        {
            return true;
        }
        return false;
    }

    // NT1
    public boolean NT1(){
        if ( tokens.get(index).CP.equals("[") ){
            index++;
            if ( OE() ){
                if ( tokens.get(index).CP.equals("]") ){
                    index++;
                    if ( NT2() ){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // NT2
    public boolean NT2(){
        if ( tokens.get(index).CP.equals("[") )
        {
            if (NT1()){
                return true;
            }
        }
        // Null
        else if( tokens.get(index).CP.equals("comma") ||  tokens.get(index).CP.equals("semicolon") )
        {
            return true;
        }
        return false;
    }

    // ALL
    public boolean ALL()
    {   
        if ( tokens.get(index).CP.equals("ID") )
        {
            index++;
            if ( OPTIONS() )
            {
                return true;
            }
        }

        // This
        else if ( tokens.get(index).CP.equals("my") )
        {
            if ( This() )
            {
                return true;
            }
        }
        // Super
        else if ( tokens.get(index).CP.equals("base") )
        {
            if ( Super() )
            {
                return true;
            }
        }
        return false;
    }

    // Options
    public boolean OPTIONS()
    {  
        // Rule 1
        if ( tokens.get(index).CP.equals("CompAsgn") 
            || tokens.get(index).CP.equals("AsgnOp") )
        {
            if ( ASGN() )
            {
                return true;
            }
        }

        // Rule 2
        else if ( tokens.get(index).CP.equals("lp") )
        {
            if ( FUNC_C() )
            {
                return true;
            }
        }

        // Rule 3
        else if ( tokens.get(index).CP.equals("ID") )
        {
            if ( OBJECT() )
            {
                return true;
            }
        }

        // Rule 4
        else if ( tokens.get(index).CP.equals("IncDec") )
        {
            index++;
            if ( tokens.get(index).CP.equals("semicolon") )
            {
                index++;
                return true;
            }
        }
        //Rule 5
        else if ( tokens.get(index).CP.equals("ref") )
        {
            index++;
            if ( R() )
            {
                return true;
            }
        }
        // RUle 6 array
        else if ( tokens.get(index).CP.equals("[") )
        {
            if ( ARR() )
            {
                if ( ARR2() )
                {
                    return true;
                }
            }
        }
        return false;
    }

    // ARR2
    public boolean ARR2()
    {
        if ( tokens.get(index).CP.equals("Asgn") || tokens.get(index).CP.equals("CompAsgn") )
        {
            if ( ASGN() )
            {
                 return true;
            }
        }

        else if ( tokens.get(index).CP.equals("ref") )
        {
            index++;
            if ( R() )
            {
                return true;
            }
        }

        // Null Rule
        else if ( tokens.get(index).CP.equals("semicolon") )
        {
            index++;
            return true;
        }

        return false;
    }

    // Function Call
    public boolean FUNC_C()
    {
        if ( tokens.get(index).CP.equals("lp") )
        {
            index++;
            if ( ARGS() )
            {
                if ( tokens.get(index).CP.equals("rp") )
                {
                    index++;
                    if ( tokens.get(index).CP.equals("semicolon") )
                    {
                        index++;
                        return true;
                    }
                }
            }
        }
        return false;
    }
    // Arguments
    public boolean ARGS()
    {
        // Arguments for functions are OE,OE,OE,...
        if ( tokens.get(index).CP.equals("int") || tokens.get(index).CP.equals("float") || 
            tokens.get(index).CP.equals("str") || tokens.get(index).CP.equals("char") ||
            tokens.get(index).CP.equals("bool") || tokens.get(index).CP.equals("ID") || 
            tokens.get(index).CP.equals("lp") || tokens.get(index).CP.equals("LogNot") 
            )
        {
            if ( OE() )
            {
                if ( ARGS1() )
                {
                    return true;
                }
            }
        }

        // Null rule 
        else if ( tokens.get(index).CP.equals("rp") )
        {
            return true;
        }
        return false;
    }

    // ARGS1
    public boolean ARGS1()
    {
        if  ( tokens.get(index).CP.equals("comma") )
        {
            index++;
            if ( OE() )
            {
                if ( ARGS1() )
                {
                    return true;
                }
            }
        }

        // Null Rule
        if ( tokens.get(index).CP.equals("rp") )
        {
            return true;
        }
        return false;
    }

    
    // Object declaration
    public boolean OBJECT()
    {
        if ( tokens.get(index).CP.equals("ID") )
        {
            index++;
            if ( M2() )
            {
                return true;
            }
        }
        return false;
    }
    
    // M2
    public boolean M2()
    {
        // Rule 1
        if ( tokens.get(index).CP.equals("AsgnOp") )
        {
            index++;
            if ( tokens.get(index).CP.equals("new") )
            {
                index++;
                if ( tokens.get(index).CP.equals("ID") )
                {
                    index++;
                    if ( tokens.get(index).CP.equals("lp") )
                    {
                        index++;
                        if  ( Any() )
                        {
                            if ( tokens.get(index).CP.equals("rp") )
                            {
                                index++;
                                if ( tokens.get(index).CP.equals("semicolon") )
                                {
                                    index++;
                                    return true;   
                                }
                            }
                        }
                    }
                }
            }
        }
        // Rule 2
        else if ( tokens.get(index).CP.equals("semicolon") )
        {
            index++;
            return true;
        }
        return false;
    }

    // ASGN
    public boolean ASGN()
    {
        if ( tokens.get(index).CP.equals("AsgnOp") )
        {
            index++;
            if ( OE() )
            {
                if ( tokens.get(index).CP.equals("semicolon") )
                {
                    index++;
                    return true;
                }
            }
        }

        else if ( tokens.get(index).CP.equals("CompAsgn") )
        {
            index++;
            if ( OE() )
            {
                if ( tokens.get(index).CP.equals("semicolon") )
                {
                    index++;
                    return true;
                }
            }        
        }
        return false;
    }

    // R
    public boolean R()
    {
        // Rule 1
        if ( tokens.get(index).CP.equals("ID") )
        {
            index++;
            if ( N() )
            {
                return true;
            }
        }
        return false;
    }

    // N
    public boolean N()
    {
        // Rule 1
        if ( tokens.get(index).CP.equals("lp") )
        {
            index++;
            if ( ARGS() )
            {
                if ( tokens.get(index).CP.equals("rp") )
                {
                    index++;
                    if ( Y() )
                    {
                        return true;
                    }
                }
            }
        }

        //RUle 2
        else if ( tokens.get(index).CP.equals("CompAsgn") || tokens.get(index).CP.equals("AsgnOp")  )
        {
            if ( ASGN() )
            {
                return true;
            }
        }

        // Rule 3
        else if ( tokens.get(index).CP.equals("ref") )
        {
            index++;
            if ( tokens.get(index).CP.equals("ID") )
            {
                index++;
                if ( Y() )
                {
                     return true;
                }
            }
        }

        // Rule 4 Array indexing
        else if ( tokens.get(index).CP.equals("[") )
        {
            if ( ARR() )
            {
                if ( L() )
                {
                     return true;
                }
            }
        }
        return false; 
    }

    // Y
    public boolean Y()
    {
        // Rule 1
        if ( tokens.get(index).CP.equals("ref") )
        {
            index++;
            if ( tokens.get(index).CP.equals("ID") )
            {
                index++;
                if ( Y() )
                {
                    return true;
                }
            }
        }

        //Rule 2
        else if ( tokens.get(index).CP.equals("lp") )
        {
            index++;
            if ( ARGS() )
            {
                if ( tokens.get(index).CP.equals("rp") )
                {
                    index++;
                    if ( Q() )
                    {
                        return true;
                    }
                }
            }
        }

        // Rule 3
        else if ( tokens.get(index).CP.equals("CompAsgn") || tokens.get(index).CP.equals("AsgnOp") )
        {
            if ( ASGN() )
            {
                return true;
            }
        }

        // Rule 4
        else if ( tokens.get(index).CP.equals("[") )
        {
            if  (ARR())
            {
                 if ( L() )
                {
                    return true;
                }
            }
        }
        return false;
    }

    // L
    public boolean L()
    {
        if ( tokens.get(index).CP.equals("CompAsgn") || tokens.get(index).CP.equals("AsgnOp") )
        {
            if ( ASGN() )
            {
                return true;
            }
        }

        // .ID
        else if ( tokens.get(index).CP.equals("ref") )
        {
            index++;
            if ( tokens.get(index).CP.equals("ID") )
            {
                index++;
                if ( Y() )
                {
                    return true;
                }
            }
        }
        // Termination
        else if ( tokens.get(index).CP.equals("semicolon") )
        {
            index++;
            return true;
        }

        return false; 
    }

    // K
    public boolean Q()
    {
        if ( tokens.get(index).CP.equals("ref") )
        {
            index++;
            if ( OE() )
            {
                if ( tokens.get(index).CP.equals("ID") )
                {
                    index++;
                    if ( Y() )
                    {
                        return true;
                    }
                }
            }
        }

        else if ( tokens.get(index).CP.equals("semicolon") )
        {
            index++;
            return true;
        }
        return false;
    }

    // ARRAY Indexing
    public boolean ARR()
    {
        if ( tokens.get(index).CP.equals("[") )
        {
            index++;
            if ( OE() )
            {
                if ( NT3() )
                {
                    return true;
                }
            }
        }
        return false;
    }

    // NT3
    public boolean NT3()
    {
        if ( tokens.get(index).CP.equals("]") )
        {
            index++;
            if ( NT4() )
            {
                return true;
            }
        }
        return false;
    }

    // NT4
    public boolean NT4()
    {
        if ( tokens.get(index).CP.equals("[") )
        {
            if ( ARR() )
            {
                return true;
            }
        }

        // Null case
        else
        {
            if ( tokens.get(index).CP.equals("ref") || tokens.get(index).CP.equals("CompAsgn") || 
                tokens.get(index).CP.equals("AsgnOp") || tokens.get(index).CP.equals("semicolon") ) 
            {
                return true;
            }
        }
        return false;
    }

    // Break and Continue
    public boolean Break()
    {
        if ( tokens.get(index).CP.equals("break") )
        {
            index++;
            if ( tokens.get(index).CP.equals("semicolon") )
            {
                index++;
                return true;
            }
        }
        return false;
    }

    public boolean Continue()
    {
        if ( tokens.get(index).CP.equals("continue") )
        {
            index++;
            if ( tokens.get(index).CP.equals("semicolon") )
            {
                index++;
                return true;
            }
        }
        return false;
    }

    // ------------------------------------ OOP & Functional -------------------------------------------------

    // --------------------------------------------------------- Functional ----------------------------------------------------------------------------------

    // While loop
    public boolean While(){
        if ( tokens.get(index).CP.equals("while") )
        {
            index++;
            if ( tokens.get(index).CP.equals("lp") ){
                index++;
                if ( OE() ){
                    if ( tokens.get(index).CP.equals("rp") ){
                        global = false; // Local scope start
                        SMA.createScope();
                        index++;
                        if ( Body() )
                        {
                            global = true; // Local scope close
                            SMA.destroyScope();
                            return true;
                        }
                    }
                }
            }

        }
        return false;
    }

    // If Else
    public boolean IF_Else(){
        if ( tokens.get(index).CP.equals("if") )
        {
            index++;
            if ( tokens.get(index).CP.equals("lp") ){
                index++;
                if ( OE() ){
                    if ( tokens.get(index).CP.equals("rp") ){
                        global = false; // Local scope start
                        SMA.createScope(); 
                        index++;
                        if ( Body() ){
                            global = true; // Local scope close
                            SMA.destroyScope();
                            if (O_Else())
                            {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;                
    }

    // Optional Else
    public boolean O_Else(){
        if ( tokens.get(index).CP.equals("else") )
        {
            global = false; // Local scope start
            SMA.createScope();
            index++;
            if ( IF_BODY() ){
                global = true; // Local scope close
                SMA.destroyScope();
                return true;
            }
        }

        // Null follow of SST plus Follow of MST 
        else {
            if ( tokens.get(index).CP.equals("rcb") || tokens.get(index).CP.equals("DT") || tokens.get(index).CP.equals("ID") || 
            tokens.get(index).CP.equals("if") || tokens.get(index).CP.equals("for") || tokens.get(index).CP.equals("while") || 
            tokens.get(index).CP.equals("func" ) || tokens.get(index).CP.equals("try") || tokens.get(index).CP.equals("return") || 
            tokens.get(index).CP.equals("break") || tokens.get(index).CP.equals("continue") || tokens.get(index).CP.equals("IncDec") ||
            tokens.get(index).CP.equals("my") || tokens.get(index).CP.equals("base")
            ){
                return true;
            }
        }
        return false;
    }

    // IF body for else
    public boolean IF_BODY()
    {
        // SST First
        if ( tokens.get(index).CP.equals("DT") || tokens.get(index).CP.equals("ID") || 
        tokens.get(index).CP.equals("if") || tokens.get(index).CP.equals("for") || tokens.get(index).CP.equals("while") || 
        tokens.get(index).CP.equals("func" ) || tokens.get(index).CP.equals("try") || tokens.get(index).CP.equals("return") || 
        tokens.get(index).CP.equals("break") || tokens.get(index).CP.equals("continue") || tokens.get(index).CP.equals("IncDec") ||
        tokens.get(index).CP.equals("my") || tokens.get(index).CP.equals("base")  
        )
        {
            if ( SST() ){
                return true;
            }
        }
        // MST
        else if ( ( tokens.get(index).CP.equals("lcb") ) )
        {
            index++;
            if ( Body2() )
            {
                if ( tokens.get(index).CP.equals("rcb") )
                {
                    index++;
                    return true;
                }
            }
        }
        return false;
    }

    // For loop
    public boolean For(){
        if ( tokens.get(index).CP.equals("for") )
        {
            index++;
            if ( tokens.get(index).CP.equals("lp") ) // for scope starts here
            {
                SMA.createScope();
                index++;
                if ( P1() ){
                    if(tokens.get(index).CP.equals("semicolon"))
                    {
                        index++;
                        if ( P2() ){
                            if(tokens.get(index).CP.equals("semicolon"))
                            {
                                index++;
                                if ( P3() ){
                                    if ( tokens.get(index).CP.equals("rp") )
                                    {

                                        index++;
                                        if ( Body() )
                                        {
                                            SMA.destroyScope(); // For scope closes
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }            
        }
        return false;
    }


    // Condition 1
    public boolean P1(){
        // Rule 1
        if ( tokens.get(index).CP.equals("DT") ){
            index++;
            if (tokens.get(index).CP.equals("ID") ){
                index++;
                if ( V() ){
                    return true;
                }
            }
        }

        // Rule 2
        else if ( tokens.get(index).CP.equals("ID") ){
            index++;
            if ( tokens.get(index).CP.equals("AsgnOp") ){
                if ( OE() )
                {
                    return true;
                }
            }
        }

        // Null
        else {  
            if ( tokens.get(index).CP.equals("semicolon") ){
                return true;
            }
        }
        return false; 
    }

    // V
    public boolean V(){
        if ( tokens.get(index).CP.equals("AsgnOp") ){
            index++;
            if (OE()){
                if ( U() ){
                    return true;
                }
            }
        }

        // ,ID
        if ( tokens.get(index).CP.equals("comma") )
        {
            if ( U() )
            {
                return true;
            }
        }
        return false;
    }

    // U
    public boolean U(){
        if ( tokens.get(index).CP.equals("comma") ){
            index++;
            if ( tokens.get(index).CP.equals("ID") ){
                index++;
                if( U() )
                {
                    if (V())
                    {
                        return true;
                    }
                }
            }
        }
        // Null First of V
        else {  
            if ( tokens.get(index).CP.equals("semicolon") || tokens.get(index).CP.equals("AsgnOp") ){
                return true;
            }
        }
        return false;
    }

    // Condtion 2
    public boolean P2(){
        if (tokens.get(index).CP.equals("int") || tokens.get(index).CP.equals("float") || tokens.get(index).CP.equals("str") || tokens.get(index).CP.equals("char") ||
            tokens.get(index).CP.equals("bool") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("lp") || tokens.get(index).CP.equals("LogNot")
         )
        {
            if ( OE() ){
                return true;
            }
        }
        // Null
        else{
            if ( tokens.get(index).CP.equals("semicolon") ){
                return true;
            }
        }
        return false;
    }

    // Condition 3
    public boolean P3(){
        if (tokens.get(index).CP.equals("ID") )
        {
            index++;
            if ( P4() ){
                if ( P5()){
                    return true;
                }
            }
        }

        // Pre Inc
        else if ( tokens.get(index).CP.equals("IncDec") )
        {
            if ( PRE_INC() )
            {
                if ( P5() )
                {
                    return true;
                }
            }
        }

        // Null
        else{
            if ( tokens.get(index).CP.equals("rp") ){
                return true;
            }
        }
        return false;
    }

    // P4
    public boolean P4(){
        
        if ( tokens.get(index).CP.equals("IncDec") )
        {
            index++;
            return true;
        }
        
        else if (tokens.get(index).CP.equals("AsgnOp"))
        {
            index++;
            if ( OE() ){
                return true;
            }
        }
        else if( tokens.get(index).CP.equals("CompAsgn") )
        {
            index++;
            if ( OE() ){
                return true;
            }
        }

        return false;
    }

    // P5
    public boolean P5()
    {
        if ( tokens.get(index).CP.equals("comma") ){
            index++;
            if ( tokens.get(index).CP.equals("ID") )
            {
                index++;
                if ( P4() ){
                    return true;
                }
            }
        }

        else if ( tokens.get(index).CP.equals("comma") ){
            index++;
            if ( PRE_INC() )
            {
                return true;
            }
        }

        // Null
        else{
            if ( tokens.get(index).CP.equals("rp") ){
                return true;
            }
        }

        return false;
    }


    // -------------------------------------------------- Expression --------------------------------------------------------------
    // lp = (, rp = ), ^^ = logical not(LogNot) 
    // OE follow = ), ;, ], ,
    // OE: param, any, if, while, for(p2, p3), dec(new2), arr(n1)=], asgn,
    /* Compatibility Checks
    L OR R: L = bool, R = bool
    L AND R: L=R=bool
    L +-*%/ R: L=R=int or float
    L + R: L=R=string
    L ROP R= bool
    str  
    */
    public boolean OE()
    {
        if ( tokens.get(index).CP.equals("int") || tokens.get(index).CP.equals("float") || tokens.get(index).CP.equals("str") || tokens.get(index).CP.equals("char") ||
            tokens.get(index).CP.equals("bool") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("lp") || tokens.get(index).CP.equals("LogNot")
        )
        {
            if (AE())
            {
                if ( OE1() )
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    // OE1
    public boolean OE1()
    {
        // Rule 1
        if ( tokens.get(index).CP.equals("AndOR") )
        {
            index++;
            if ( AE() )
            {
                if ( OE1() )
                {
                    return true;
                }
            }
        }

        // Null
        else {
            // Follow set of OE1 = F(OE)
            if ( tokens.get(index).CP.equals("rp") || tokens.get(index).CP.equals("comma") || 
                tokens.get(index).CP.equals("semicolon") || tokens.get(index).CP.equals("]")
                )
            {
                return true;
            }
        }
        return false;            
    }

    // AE
    public boolean AE()
    {
        if ( tokens.get(index).CP.equals("int") || tokens.get(index).CP.equals("float") || tokens.get(index).CP.equals("str") || tokens.get(index).CP.equals("char") ||
        tokens.get(index).CP.equals("bool") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("lp") || tokens.get(index).CP.equals("LogNot") )
        {
            if ( RE() )
            {
                if ( AE1() )
                {
                    return true;
                }
            }
        }
        return false;
    }

    // AE1
    public boolean AE1()
    {
        // Rule 1
        if ( tokens.get(index).CP.equals("AndOR") )
        {
            index++;
            if ( RE() )
            {
                if ( AE1() )
                {
                    return true;
                }
            }
        }

        // Null
        else {
            // Follow set of OE plus remaining operators
            if ( tokens.get(index).CP.equals("rp") || tokens.get(index).CP.equals("comma") || 
                tokens.get(index).CP.equals("semicolon") || tokens.get(index).CP.equals("]") || 
                tokens.get(index).CP.equals("AndOR")
                )
            {
                return true;
            }
        }
        return false;            
    }

    // RE
    public boolean RE()
    {
        if ( tokens.get(index).CP.equals("int") || tokens.get(index).CP.equals("float") || tokens.get(index).CP.equals("str") || tokens.get(index).CP.equals("char") ||
        tokens.get(index).CP.equals("bool") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("lp") || tokens.get(index).CP.equals("LogNot") )
        {
            if ( E() )
            {
                if ( RE1() )
                {
                    return true;
                }
            }
        }
        return false;
    }

    // RE1
    public boolean RE1()
    {
        // Rule 1
        if ( tokens.get(index).CP.equals("ROP") )
        {
            index++;
            if ( E() )
            {
                if ( RE1() )
                {
                    return true;
                }
            }
        }

        // Null
        else {
            // Follow set of OE
            if ( tokens.get(index).CP.equals("rp") || tokens.get(index).CP.equals("comma") || 
                tokens.get(index).CP.equals("semicolon") || tokens.get(index).CP.equals("]") ||
                tokens.get(index).CP.equals("AndOR")
                )
            {
                return true;
            }
        }
        return false;            
    }

    // E
    public boolean E()
    {
        if ( tokens.get(index).CP.equals("int") || tokens.get(index).CP.equals("float") || tokens.get(index).CP.equals("str") || tokens.get(index).CP.equals("char") ||
        tokens.get(index).CP.equals("bool") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("lp") || tokens.get(index).CP.equals("LogNot") )
        {
            if ( TF() )
            {
                if ( E1() )
                {
                    return true;
                }
            }
        }
        return false;
    }

    // E1
    public boolean E1()
    {
        // Rule 1
        if ( tokens.get(index).CP.equals("PM") )
        {
            index++;
            if ( TF() )
            {
                if ( E1() )
                {
                    return true;
                }
            }
        }

        // Null
        else {
            // Follow set of OE plus remaining operators
            if (tokens.get(index).CP.equals("rp") || tokens.get(index).CP.equals("comma") || 
                tokens.get(index).CP.equals("semicolon") || tokens.get(index).CP.equals("]") ||
                tokens.get(index).CP.equals("ROP") || tokens.get(index).CP.equals("AndOR") 
                )
            {
                return true;
            }
        }
        return false;            
    }

    // T
    public boolean TF()
    {
        if ( tokens.get(index).CP.equals("int") || tokens.get(index).CP.equals("float") || tokens.get(index).CP.equals("str") || 
            tokens.get(index).CP.equals("char") || tokens.get(index).CP.equals("bool") || tokens.get(index).CP.equals("ID") || 
            tokens.get(index).CP.equals("lp") || tokens.get(index).CP.equals("LogNot")
            )
        {
            if ( F() )
            {
                if ( TF1() )
                {
                    return true;
                }
            }
        }
        return false;
    }

    // T1
    public boolean TF1()
    {
        // Rule 1
        if ( tokens.get(index).CP.equals("MDM") )
        {
            index++;
            if ( F() )
            {
                if ( TF1() )
                {
                    return true;
                }
            }
        }

        // Null
        else {
            // Follow set of OE
            if (tokens.get(index).CP.equals("rp") || tokens.get(index).CP.equals("comma") || 
                tokens.get(index).CP.equals("semicolon") || tokens.get(index).CP.equals("]") ||
                tokens.get(index).CP.equals("ROP") || tokens.get(index).CP.equals("PM") || 
                tokens.get(index).CP.equals("AndOR") )
            {
                return true;
            }
        }
        return false;            
    }

    // F
    public boolean F()
    {
        // Rule 0
        if ( tokens.get(index).CP.equals("int") || tokens.get(index).CP.equals("float") || tokens.get(index).CP.equals("str") || tokens.get(index).CP.equals("char") ||
        tokens.get(index).CP.equals("bool") )
        {
            if ( CONST() )
            {
                return true;
            }

        }
        
        // Rule 1
        if ( tokens.get(index).CP.equals("ID") )
        {
            index++;
            if ( O() )
            {
                if ( DOT() )
                {
                    return true;
                }
            }
        }

        // Rule 2
        else if ( tokens.get(index).CP.equals("lp") )
        {
            index++;
            if ( OE() )
            {
                if ( tokens.get(index).CP.equals("rp") )
                {
                    index++;
                    return true;
                }
            }
        }
        // Rule 3
        else if ( tokens.get(index).CP.equals("LogNot") )
        {
            index++;
            if ( F() )
            {
                return true;
            }
        }
        return false;
    }

    // O
    public boolean O()
    {
        // Rule 1 
        if ( tokens.get(index).CP.equals("[") )
        {
            if ( ARR() )
            {
                return true;
            }
        }
        // Rule 2
        else if( tokens.get(index).CP.equals("lp") )
        {
            index++;
            if ( ARGS() )
            {
                if ( tokens.get(index).CP.equals("rp") )
                {
                    index++;
                    return true;
                }
            }
        }

        // Null case
        else if ( tokens.get(index).CP.equals("rp") || tokens.get(index).CP.equals("comma") || 
            tokens.get(index).CP.equals("semicolon") || tokens.get(index).CP.equals("]") ||
            tokens.get(index).CP.equals("ROP") || tokens.get(index).CP.equals("PM") || 
            tokens.get(index).CP.equals("MDM") ||tokens.get(index).CP.equals("AndOR") ||
            tokens.get(index).CP.equals("ref")
            )
            {
                return true;
            }

        return false;
    }

    // DOT
    public boolean DOT()
    {
        if ( tokens.get(index).CP.equals("ref") )
        {
            index++;
            if ( tokens.get(index).CP.equals("ID") )
            {
                index++;
                if ( O() )
                {
                    if ( DOT() )
                    {
                        return true;
                    }
                }
            }
        }
        // Null rule
        else if ( tokens.get(index).CP.equals("rp") || tokens.get(index).CP.equals("comma") || 
        tokens.get(index).CP.equals("semicolon") || tokens.get(index).CP.equals("]") ||
        tokens.get(index).CP.equals("ROP") || tokens.get(index).CP.equals("PM") || 
        tokens.get(index).CP.equals("MDM") ||tokens.get(index).CP.equals("AndOR")
        )
        {
            return true;
        }

        return false;
    }
    // -------------------------------------------------- Expression --------------------------------------------------------------

    // Funtion ----------------------------------------------------
    
    // Return 
    public boolean Return(){
        if ( tokens.get(index).CP.equals("return") )
        {
            index++;
            if ( Any() ){
                return true;
            }
        }
        return false;
    }

    // Any
    // Follow of Any = F(return)=F(SST)= }, ? U {, ), ;,} 
    // Also change Any_O
    public boolean Any(){
        // Selection set this OE
        if ( tokens.get(index).CP.equals("int") || tokens.get(index).CP.equals("float") || tokens.get(index).CP.equals("str") || tokens.get(index).CP.equals("char") ||
        tokens.get(index).CP.equals("bool") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("lp") || tokens.get(index).CP.equals("LogNot") || 
        tokens.get(index).CP.equals("IncDec"))
        {
            if ( OE() ){
                return true;
            }
        }
        // Null
        else if ( tokens.get(index).CP.equals("rcb") || tokens.get(index).CP.equals("rp") ||
                tokens.get(index).CP.equals("semicolon") || tokens.get(index).CP.equals("?") 
                )
        {
            return true;
        }
        return false;
    }
    
    public boolean Func()
    {
        if ( tokens.get(index).CP.equals("func" ) )
        {
            type = "Function"; // Entity type
            index++;
            if ( tokens.get(index).CP.equals("ID" ) )
            {
                name = tokens.get(index).value; // Function name
                index++;
                if ( tokens.get(index).CP.equals("lp" ) ) // Function scope starts here
                {
                    SMA.createScope();
                    function = true;
                    String params = ""; // To store Parameters's types 
                    index++;
                    if ( Param(params) )
                    {
                        // Insert into Main table
                        // Check if duplicate
                        if ( ! SMA.insertMT(name, type, params, CM, parent, Class) )
                        {
                            System.out.println("Duplicate Function Error");
                            Semantic = true;
                            return false;
                        }
                        name = null;
                        type = null;
                        CM = null;
                        if  ( tokens.get(index).CP.equals("rp" ) )
                        {
                            index++;
                            if ( Func_Body() )
                            {
                                SMA.destroyScope(); // Function scope closes
                                function = false;
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    // Parameters
    public boolean Param(String params)
    {
        if ( tokens.get(index).CP.equals("DT" ) )
        {
            DT = tokens.get(index).value; // DT of parameter
            params = params + DT; // Add it to params list
            index++;
            if ( P(params) )
            {
                return true;
            }
        }

        // Null
        else
        {
            if ( tokens.get(index).CP.equals("rp" ) )
            {
                return true;
            }
        }
        return true;
    }

    // P
    public boolean P(String params)
    {
        // Rule 1
        if ( tokens.get(index).CP.equals("ID" ) )
        {
            index++;
            if ( PL(params) )
            {
                return true;
            }
        }
        
        // Rule 2
        else if ( tokens.get(index).CP.equals("[" ) )
        {
            index++;
            if ( tokens.get(index).CP.equals("]" ) )
            {
                index++;
                if ( PO() )
                {
                    if (  tokens.get(index).CP.equals("ID" ) )
                    {
                        index++;
                        if  (PL(params) )
                        {
                             return true;
                        }
                    }
                }
            }
        }
        // Null
        else
        {
            if ( tokens.get(index).CP.equals("rp" ) )
            {
                return true;
            }
        }
        return false;
    }

    // PO
    public boolean PO()
    {
        if (  tokens.get(index).CP.equals("[" )  )
        {
            index++;
            if (  tokens.get(index).CP.equals("]" ) )
            {
                index++;
                if ( PO() )
                {
                    return true;
                }
            }
        }

        // Null
        else if (  tokens.get(index).CP.equals("ID" ) )
        {
            return true;
        }
        return false;
    }

    // PL
    public boolean PL(String params)
    {
        // Rule 2
        if ( tokens.get(index).CP.equals("comma" ) )
        {
            index++;
            if (  tokens.get(index).CP.equals("DT" )  )
            {
                DT = tokens.get(index).value; // DT of parameter
                params = params +","+ DT; // Add it to params list with ,
                index++;
                if ( PL1(params) )
                {
                    return true;
                }
            }
        }

        // Null
        else
        {
            if ( tokens.get(index).CP.equals("rp" ) )
            {
                return true;
            }
        }
        return false;
    }

    // PL1 
    public boolean PL1(String params)
    {
        // Rule 1
        if ( tokens.get(index).CP.equals("ID" ) )
        {
            index++;
            if ( PL(params) )
            {
                return true;
            }
        }
        
        // Rule 2
        else if ( tokens.get(index).CP.equals("[" ) )
        {
            index++;
            if ( tokens.get(index).CP.equals("]" ) )
            {
                index++;
                if ( PO() )
                {
                    if (  tokens.get(index).CP.equals("ID" ) )
                    {
                        index++;
                        if  (PL(params) )
                        {
                             return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    // Funtion ----------------------------------------------------
    
    // ---------------- Functional-------------------------------------

    // CONST
    public boolean CONST()
    {
        if ( tokens.get(index).CP.equals("int") || tokens.get(index).CP.equals("float") || tokens.get(index).CP.equals("char") ||
            tokens.get(index).CP.equals("str") || tokens.get(index).CP.equals("bool")
            )
        {
            index++;
            return true;
        }
        return false;
    }
    
    // TRY
    public boolean RUN_GRAB()
    {
        if ( tokens.get(index).CP.equals("try") )
        {
            index++;
            if ( tokens.get(index).CP.equals("lcb") )
            {
                index++;
                if ( TBody() )
                {
                    if ( tokens.get(index).CP.equals("rcb") )
                    {
                        index++;
                        if ( GRB() )
                        {   
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // Try Catch Body
    public boolean TBody()
    {
        // MST
        if ( tokens.get(index).CP.equals("DT") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("if") || 
        tokens.get(index).CP.equals("for") || tokens.get(index).CP.equals("while" ) || tokens.get(index).CP.equals("func" ) || 
        tokens.get(index).CP.equals("try") || tokens.get(index).CP.equals("return") || tokens.get(index).CP.equals("break") || 
        tokens.get(index).CP.equals("continue") || tokens.get(index).CP.equals("IncDec") || tokens.get(index).CP.equals("my") ||
        tokens.get(index).CP.equals("base") )
        {
            if ( MST() )
            {
                return true;
            }
        }
        // Semicolon
        else if ( tokens.get(index).CP.equals("semicolon") )
        {
            index++;
            return true;
        }

        // Null
        else if ( tokens.get(index).CP.equals("rcb") )
        {
            return true;
        }
        return false;
    }

    // GRB
    public boolean GRB()
    {
        if ( tokens.get(index).CP.equals("grab") )
        {
            if ( GRAB() )
            {
                if ( FINAL() )
                {
                    return true;
                }
            }
        }

        else if ( tokens.get(index).CP.equals("finally") )
        {
            index++;
            if ( tokens.get(index).CP.equals("lcb") )
            {
                index++;
                if ( TBody() )
                {
                    if ( tokens.get(index).CP.equals("rcb") )
                    {
                        index++;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // GRAB
    public boolean GRAB()
    {
        if ( tokens.get(index).CP.equals("grab") )
        {
            index++;
            if ( OPT2() )
            {
                return true;
            }
        }
        return false;
    }

    // OPT2
    public boolean OPT2()
    {
        if ( tokens.get(index).CP.equals("lcb") )
        {
            index++;
            if ( TBody() )
            {
                if ( tokens.get(index).CP.equals("rcb") )
                {
                    index++;
                    return true;
                } 
            }
        }
        
        else if ( tokens.get(index).CP.equals("lp") )
        {
            index++;
            if ( tokens.get(index).CP.equals("ID") )
            {
                index++;
                if ( tokens.get(index).CP.equals("ID") )
                {
                    index++;
                    if ( tokens.get(index).CP.equals("rp") )
                    {
                        index++;
                        if ( tokens.get(index).CP.equals("lcb") )
                        {
                            index++;
                            if ( TBody() )
                            {
                                if ( tokens.get(index).CP.equals("rcb") )
                                {
                                    index++;
                                    if ( OPT3() )
                                    {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    // OPT3
    public boolean OPT3()
    {
        if ( tokens.get(index).CP.equals("grab") )
        {
            index++;
            if ( tokens.get(index).CP.equals("lp") )
            {
                index++;
                if ( tokens.get(index).CP.equals("ID") )
                {
                    index++;
                    if ( tokens.get(index).CP.equals("ID") )
                    {
                        index++;
                        if ( tokens.get(index).CP.equals("rp") )
                        {
                            index++;
                            if ( tokens.get(index).CP.equals("lcb") )
                            {
                                index++;
                                if ( TBody() )
                                {
                                    if ( tokens.get(index).CP.equals("rcb") )
                                    {
                                        index++;
                                        if ( OPT3() )
                                        {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Null
        else 
        {
            if ( tokens.get(index).CP.equals("finally") || tokens.get(index).CP.equals("DT") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("if") || 
            tokens.get(index).CP.equals("for") || tokens.get(index).CP.equals("while" ) || tokens.get(index).CP.equals("func" ) || 
            tokens.get(index).CP.equals("try") || tokens.get(index).CP.equals("return") || tokens.get(index).CP.equals("break") || 
            tokens.get(index).CP.equals("continue") || tokens.get(index).CP.equals("IncDec") || tokens.get(index).CP.equals("my") ||
            tokens.get(index).CP.equals("base")                
            )
            {
                return true;
            }
        }
        return false;
    }
     
    // Finally
    public boolean FINAL()
    {
        if ( tokens.get(index).CP.equals("finally") )
        {
            index++;
            if ( tokens.get(index).CP.equals("lcb") )
            {
                index++;
                if ( TBody() )
                {
                    if ( tokens.get(index).CP.equals("rcb") )
                    {
                        index++;
                        return true;
                    }
                }
            }
        }
        // Null
        else {
            if ( tokens.get(index).CP.equals("DT") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("if") || 
            tokens.get(index).CP.equals("for") || tokens.get(index).CP.equals("while" ) || tokens.get(index).CP.equals("func" ) || 
            tokens.get(index).CP.equals("try") || tokens.get(index).CP.equals("return") || tokens.get(index).CP.equals("break") || 
            tokens.get(index).CP.equals("continue") || tokens.get(index).CP.equals("IncDec") || tokens.get(index).CP.equals("my") ||
            tokens.get(index).CP.equals("base") )
            {
                return true;
            }
        }
        return false;
    }


    // Body for while for function
    public boolean Body()
    {
        if ( tokens.get(index).CP.equals("DT") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("if") || 
        tokens.get(index).CP.equals("for") || tokens.get(index).CP.equals("while" ) || tokens.get(index).CP.equals("func" ) || 
        tokens.get(index).CP.equals("try") || tokens.get(index).CP.equals("return") || tokens.get(index).CP.equals("break") || 
        tokens.get(index).CP.equals("continue") || tokens.get(index).CP.equals("IncDec") || tokens.get(index).CP.equals("my") ||
        tokens.get(index).CP.equals("base")
        )
        {
            if ( SST() )
            {
                return true;
            }
        }

        else if ( tokens.get(index).CP.equals("lcb") )
        {
            index++;
            if ( Body2() )
            {
                if ( tokens.get(index).CP.equals("rcb") )
                {
                    index++;
                    return true;
                }
            }
        }

        // Semicolon 
        else {
            if ( tokens.get(index).CP.equals("semicolon") )
            {
                index++;
                return true;
            }
        }
        return false;
    }

    // Body 2
    public boolean Body2()
    {
        // FS(MST) + F(MST)
        if ( tokens.get(index).CP.equals("DT") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("if") || 
        tokens.get(index).CP.equals("for") || tokens.get(index).CP.equals("while" ) || tokens.get(index).CP.equals("func" ) || 
        tokens.get(index).CP.equals("try") || tokens.get(index).CP.equals("return") || tokens.get(index).CP.equals("break") || 
        tokens.get(index).CP.equals("continue") || tokens.get(index).CP.equals("IncDec") || tokens.get(index).CP.equals("my") ||
        tokens.get(index).CP.equals("base") || tokens.get(index).CP.equals("rcb") )
        {
            if ( MST() )
            {
                return true;
            }
        }

        else if ( tokens.get(index).CP.equals("semicolon") )
        {
            index++;
            if ( B2() )
            {
                return true;
            }
        }
        return false;
    }

    // B3
    public boolean B2()
    {
        if ( tokens.get(index).CP.equals("semicolon") )
        {
            index++;
            if ( B2() )
            {
                return true;
            }
        }
        
        // Null
        else if ( tokens.get(index).CP.equals("rcb") )
        {
            return true;
        }
        return false;
    }

    // MST
    public boolean MST()
    {
        if ( tokens.get(index).CP.equals("DT") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("if") || 
        tokens.get(index).CP.equals("for") || tokens.get(index).CP.equals("while" ) || tokens.get(index).CP.equals("func" ) || 
        tokens.get(index).CP.equals("try") || tokens.get(index).CP.equals("return") || tokens.get(index).CP.equals("break") || 
        tokens.get(index).CP.equals("continue") || tokens.get(index).CP.equals("IncDec") || tokens.get(index).CP.equals("my") ||
        tokens.get(index).CP.equals("base")
        )
        {
            if ( SST() )
            {
                if ( MST() )
                {
                    return true;
                }
            }
        }
        
        // Null
        else
        {
            if ( tokens.get(index).CP.equals("rcb") )
            {
                return true;
            }
        }
        return false;
    }

    // SST
    public boolean SST()
    {
            if (tokens.get(index).CP.equals("while"))
            {
                if ( While() )
                {
                    return true;
                }
            }

            else if (tokens.get(index).CP.equals("if"))
            {
                if ( IF_Else() )
                {
                    return true;
                }
            }

            else if (tokens.get(index).CP.equals("for"))
            {
                if ( For() )
                {
                    return true;
                }
            }

            else if (tokens.get(index).CP.equals("func"))
            {
                if ( Func() )
                {
                    return true;
                }
            }

            else if (tokens.get(index).CP.equals("DT"))
            {
                if ( DEC() )
                {
                    return true;
                }
            }

            else if (tokens.get(index).CP.equals("ID"))
            {
                if ( ALL() )
                {
                    return true;
                }
            }

            else if (tokens.get(index).CP.equals("break"))
            {
                if ( Break() )
                {
                    return true;
                }
            }

            else if (tokens.get(index).CP.equals("continue"))
            {
                if ( Continue() )
                {
                    return true;
                }
            }

            else if (tokens.get(index).CP.equals("try"))
            {
                if ( RUN_GRAB() )
                {
                    return true;
                }
            }

            else if (tokens.get(index).CP.equals("return"))
            {
                if ( Return() )
                {
                    return true;
                }
            }

            else if (tokens.get(index).CP.equals("IncDec"))
            {
                if ( PRE_INC() )
                {
                    return true;
                }
            }

            else if (tokens.get(index).CP.equals("my"))
            {
                if ( This() )
                {
                    return true;
                }
            }

            else if (tokens.get(index).CP.equals("base"))
            {
                if ( Super() )
                {
                    return true;
                }
            }
            return false;
    }
    // Pre Increment
    public boolean PRE_INC(){
        if (tokens.get(index).CP.equals("IncDec"))
        {
            index++;
            if ( tokens.get(index).CP.equals("ID") ){
                name = tokens.get(index).value;
                // Check if Variable is declared in Scope stack
                // Check in Local table
                int stackInd = SMA.currentscope.size()-1;
                String local = null;
                while (stackInd >= 0)
                {
                    local = SMA.lookupLT(name, SMA.currentscope.get(stackInd));
                    if ( local.equals(null) )
                    {
                        // Check in the upper scope
                        stackInd--;
                    }
                    else
                    {
                        if ( ! (local.equals("int") || local.equals("float")) )
                        {
                            // typemistach error
                            System.out.println("Cannot apply pre-increment operator on type: " + local);
                            Semantic = true;
                            return false;
                        }
                        break;
                    }
                }
                // Check in MT
                if ( local.equals(null) )
                {
                    MainTable var = SMA.lookupMT(name);
                    if ( var != null )
                    {
                        if ( ! (var.DT.equals("int") || var.DT.equals("float")) )
                        {
                            // typemistach error
                            System.out.println("Cannot apply pre-increment operator on type: " + var.DT);
                            Semantic = true;
                            return false;
                        }
                    }
                    // Varible Undefined
                    else
                    {
                        System.out.println("Variable "+ name + " is undefined");
                        Semantic = true;
                        return false;
                    }
                }
                index++;
                return true;
            }
        }
        return false;
    }

    //--------------------- General expression without ID------------------------------------------------
    /* 
    public boolean OE()
    {
        if ( tokens.get(index).CP.equals("int") || tokens.get(index).CP.equals("float") || tokens.get(index).CP.equals("str") || tokens.get(index).CP.equals("char") ||
            tokens.get(index).CP.equals("bool") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("lp") || tokens.get(index).CP.equals("LogNot")
        )
        {
            if (AE())
            {
                if ( OE1() )
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    // OE1
    public boolean OE1()
    {
        // Rule 1
        if ( tokens.get(index).CP.equals("AndOR") )
        {
            index++;
            if ( AE() )
            {
                if ( OE1() )
                {
                    return true;
                }
            }
        }

        // Null
        else {
            // Follow set of OE1 = F(OE)
            if ( tokens.get(index).CP.equals("rp") || tokens.get(index).CP.equals("comma") || 
                tokens.get(index).CP.equals("semicolon") || tokens.get(index).CP.equals("]")
                )
            {
                return true;
            }
        }
        return false;            
    }

    // AE
    public boolean AE()
    {
        if ( tokens.get(index).CP.equals("int") || tokens.get(index).CP.equals("float") || tokens.get(index).CP.equals("str") || tokens.get(index).CP.equals("char") ||
        tokens.get(index).CP.equals("bool") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("lp") || tokens.get(index).CP.equals("LogNot") )
        {
            if ( RE() )
            {
                if ( AE1() )
                {
                    return true;
                }
            }
        }
        return false;
    }

    // AE1
    public boolean AE1()
    {
        // Rule 1
        if ( tokens.get(index).CP.equals("AndOR") )
        {
            index++;
            if ( RE() )
            {
                if ( AE1() )
                {
                    return true;
                }
            }
        }

        // Null
        else {
            // Follow set of OE plus remaining operators
            if ( tokens.get(index).CP.equals("rp") || tokens.get(index).CP.equals("comma") || 
                tokens.get(index).CP.equals("semicolon") || tokens.get(index).CP.equals("]") || 
                tokens.get(index).CP.equals("AndOR")
                )
            {
                return true;
            }
        }
        return false;            
    }

    // RE
    public boolean RE()
    {
        if ( tokens.get(index).CP.equals("int") || tokens.get(index).CP.equals("float") || tokens.get(index).CP.equals("str") || tokens.get(index).CP.equals("char") ||
        tokens.get(index).CP.equals("bool") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("lp") || tokens.get(index).CP.equals("LogNot") )
        {
            if ( E() )
            {
                if ( RE1() )
                {
                    return true;
                }
            }
        }
        return false;
    }

    // RE1
    public boolean RE1()
    {
        // Rule 1
        if ( tokens.get(index).CP.equals("ROP") )
        {
            index++;
            if ( E() )
            {
                if ( RE1() )
                {
                    return true;
                }
            }
        }

        // Null
        else {
            // Follow set of OE
            if ( tokens.get(index).CP.equals("rp") || tokens.get(index).CP.equals("comma") || 
                tokens.get(index).CP.equals("semicolon") || tokens.get(index).CP.equals("]") ||
                tokens.get(index).CP.equals("AndOR")
                )
            {
                return true;
            }
        }
        return false;            
    }

    // E
    public boolean E()
    {
        if ( tokens.get(index).CP.equals("int") || tokens.get(index).CP.equals("float") || tokens.get(index).CP.equals("str") || tokens.get(index).CP.equals("char") ||
        tokens.get(index).CP.equals("bool") || tokens.get(index).CP.equals("ID") || tokens.get(index).CP.equals("lp") || tokens.get(index).CP.equals("LogNot") )
        {
            if ( TF() )
            {
                if ( E1() )
                {
                    return true;
                }
            }
        }
        return false;
    }

    // E1
    public boolean E1()
    {
        // Rule 1
        if ( tokens.get(index).CP.equals("PM") )
        {
            index++;
            if ( TF() )
            {
                if ( E1() )
                {
                    return true;
                }
            }
        }

        // Null
        else {
            // Follow set of OE plus remaining operators
            if (tokens.get(index).CP.equals("rp") || tokens.get(index).CP.equals("comma") || 
                tokens.get(index).CP.equals("semicolon") || tokens.get(index).CP.equals("]") ||
                tokens.get(index).CP.equals("ROP") || tokens.get(index).CP.equals("AndOR") 
                )
            {
                return true;
            }
        }
        return false;            
    }

    // T
    public boolean TF()
    {
        if ( tokens.get(index).CP.equals("int") || tokens.get(index).CP.equals("float") || tokens.get(index).CP.equals("str") || 
            tokens.get(index).CP.equals("char") || tokens.get(index).CP.equals("bool") || tokens.get(index).CP.equals("ID") || 
            tokens.get(index).CP.equals("lp") || tokens.get(index).CP.equals("LogNot")
            )
        {
            if ( F() )
            {
                if ( TF1() )
                {
                    return true;
                }
            }
        }
        return false;
    }

    // T1
    public boolean TF1()
    {
        // Rule 1
        if ( tokens.get(index).CP.equals("MDM") )
        {
            index++;
            if ( F() )
            {
                if ( TF1() )
                {
                    return true;
                }
            }
        }

        // Null
        else {
            // Follow set of OE
            if (tokens.get(index).CP.equals("rp") || tokens.get(index).CP.equals("comma") || 
                tokens.get(index).CP.equals("semicolon") || tokens.get(index).CP.equals("]") ||
                tokens.get(index).CP.equals("ROP") || tokens.get(index).CP.equals("PM") || 
                tokens.get(index).CP.equals("AndOR") )
            {
                return true;
            }
        }
        return false;            
    }

    // F
    public boolean F()
    {
        // Rule 0
        if ( tokens.get(index).CP.equals("int") || tokens.get(index).CP.equals("float") || tokens.get(index).CP.equals("str") || tokens.get(index).CP.equals("char") ||
        tokens.get(index).CP.equals("bool") )
        {
            if ( CONST() )
            {
                return true;
            }
        }
    }
    */
}