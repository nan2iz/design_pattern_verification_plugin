package cs585.nanwarin.plugin.dvt.popup.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.Position;


import cs585.nanwarin.plugin.dvt.Activator;

public class AST_FactoryPattern extends ASTVisitor{

	FactoryPatternObjs factoryPatternObjs;
	String factoryObj;
	CompilationUnit unit;
	static ICompilationUnit iUnit;

	public AST_FactoryPattern(FactoryPatternObjs factoryPatternObjs, CompilationUnit unit, ICompilationUnit iUnit){
		this.factoryPatternObjs = factoryPatternObjs;
		this.factoryObj = null;
		this.unit = unit;
		this.iUnit = iUnit;
		}
	
	public boolean visit(VariableDeclarationFragment node) {
		SimpleName name = node.getName();
		Expression expression = node.getInitializer();
		String checkFactoryObj = null;
		
		String errorMessage  = "";

	
		try {
			checkFactoryObj = factoryPatternObjs.getFactoryObj().getChildren()[1].getElementName();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//check the variable should not start with upper case
		if(Character.isUpperCase(name.getIdentifier().charAt(0))){
			String newName = "";
			newName += Character.toLowerCase(name.getIdentifier().charAt(0));
			newName += name.getIdentifier().substring(1);
			errorMessage = "Variable name should not start with Upper Case --> [ " + name.getIdentifier() + " ], correct to --> " + newName;
			
			Position newPosition = new Position(node.getStartPosition());	
			createMarker(errorMessage, newPosition);		
		}
		
		
		if(expression.toString().contains(checkFactoryObj)){
			factoryObj = name.getIdentifier();
		}else if(factoryObj == null){
			//Highlight no implement obj;
		}else if(!expression.toString().contains(factoryObj)){
			//Call wrongly
			
			System.out.println("Wrong pattern applied to --> " + name.getIdentifier() + "; This statement is wrong -->" + expression.toString());
			System.out.println("Line number --> " + unit.getLineNumber(node.getStartPosition()));
			
			errorMessage = "Wrong pattern has been applied !!!!   Wrong statement --> [ " + expression.toString() + " ]";
			
			Position newPosition = new Position(node.getStartPosition());	
			createMarker(errorMessage, newPosition);	
			
			//Add another mark to show to correction
			errorMessage = "To correct, change to --> " + name.getIdentifier() + " = new " + factoryObj + "();";
			createMarker(errorMessage, newPosition);
		}
		
		return false; // do not continue 
	}
	

	public static void createMarker(String errorMessage, Position pos){
		IWorkspace space = ResourcesPlugin.getWorkspace();	
		IFile input = (IFile)space.getRoot().findMember(iUnit.getPath());
	//	TextSelection selection = MyMarkerFactory.getTextSelection(); // --> Get from active editor
		IMarker marker = new MyMarkerFactory().createMarker(input, pos, errorMessage);
		MyMarkerFactory.addAnnotation(marker, pos, Activator.getEditor());
	}
	
	
	/*
	public boolean visit(TypeDeclaration typeNote) {
		
		System.out.println("------------------methods-----------");
		MethodDeclaration[] notes = typeNote.getMethods();
		for(MethodDeclaration note: notes){

			if(note.getModifiers() > 0){
				System.out.println("modifier: "+note.getModifiers()); // 9 = public static,  1=public, 
				System.out.println("name: "+note.getName());
				System.out.println("parameter: " + note.parameters());
				System.out.println("return type: "+note.getReturnType2());

				System.out.println("-------");
			}


		}

		System.out.println("------------------fields-----------");
		FieldDeclaration[] nodes = typeNote.getFields();
		for(FieldDeclaration node: nodes){


			System.out.println(node.getModifiers());//25 = public static final 
			System.out.println(node.fragments());

			for(Object f: node.fragments()){
				System.out.println(f);

				String s = f.toString();
				if(s.contains("=")){
					String[] arr = s.split("=");
					s = arr[0];
				}else{

				}
			}
		}

		return false;
	}	
		*/
	
	

	public void process(CompilationUnit unit) {
		unit.accept(this);
	}
	
}
