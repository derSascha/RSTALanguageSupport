package org.fife.rsta.ac.js.ecma.api.ecma3;

import org.fife.rsta.ac.js.ecma.api.ecma3.functions.JSFunctionFunctions;


/**
 * Object Function
 * @since Standard ECMA-262 3rd. Edition
 */
public abstract class JSFunction implements JSFunctionFunctions {

    /**
     * Object Function()
     * @constructor
     * @extends Object
     * @since Standard ECMA-262 3rd. Edition
     * @since Level 2 Document Object Model Core Definition.
    */
    public JSFunction(){
	
    }
    
    /**
     * <b>property length</b>
     * @type    Number
     * @see  org.fife.rsta.ac.js.ecma.api.ecma3.JSNumber Number
     * @since   Standard ECMA-262 3rd. Edition 
     * @since   Level 2 Document Object Model Core Definition.    
    */ 
   protected JSNumber length;
   
   /**
    * <b>property prototype</b>
    * 
    * @type Function
    * @memberOf Function
    * @see  org.fife.rsta.ac.js.ecma.api.ecma3.JSFunction Function
    * @since Standard ECMA-262 3rd. Edition
    * @since Level 2 Document Object Model Core Definition.
    */
   public JSFunction protype;
   
   /**
    * <b>property constructor</b>
    * 
    * @type Function
    * @memberOf Function
    * @see  org.fife.rsta.ac.js.ecma.api.ecma3.JSFunction Function
    * @since Standard ECMA-262 3rd. Edition
    * @since Level 2 Document Object Model Core Definition.
    */
   protected JSFunction constructor;
   
}