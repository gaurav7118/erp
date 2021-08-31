/**
 * @constructor
 * @class
 * Base class for all exceptions in the toolkit
 *
 * @author Ross Dargahi
 *
 * @param {string} msg Human readable message (optional)
 * @param {string|number} code Any error or fault code (optional)
 * @param {string} method Name of the method throwing the exception (optional)
 * @param {string} detail Any additional detail (optional)
 */
function AjxException(msg, code, method, detail){
    if (arguments.length == 0) 
        return;
    
    /** Human readable message if applicable
     * @type string*/
    this.msg = msg;
    
    /** error or fault code if applicable
     * @type string|number*/
    this.code = code;
    
    /** Name of the method throwing the exception if applicable
     * @type string*/
    this.method = method;
    
    /** Any additional detail
     * @type string*/
    this.detail = detail;
}

/**
 * This method returns this class' name. Subclasses will
 * override this method to return their own name
 *
 * @return class name
 * @type String
 */
AjxException.prototype.toString = function(){
    return "AjxException";
}

/**
 * @return A string representing the state of the exception
 * @type string
 */
AjxException.prototype.dump = function(){
    return "AjxException: msg=" + this.msg + " code=" + this.code + " method=" + this.method + " detail=" + this.detail;
}

/** Invalid parent exception code
 * @type string */
AjxException.INVALIDPARENT = "AjxException.INVALIDPARENT";

/** Invalid operation exception code
 * @type string */
AjxException.INVALID_OP = "AjxException.INVALID_OP";

/** Internal error exception code
 * @type string */
AjxException.INTERNAL_ERROR = "AjxException.INTERNAL_ERROR";

/** Invalid parameter to method/operation exception code
 * @type string */
AjxException.INVALID_PARAM = "AjxException.INVALID_PARAM";

/** Unimplemented method called exception code
 * @type string */
AjxException.UNIMPLEMENTED_METHOD = "AjxException.UNIMPLEMENTED_METHOD";

/** Network error exception code
 * @type string */
AjxException.NETWORK_ERROR = "AjxException.NETWORK_ERROR";

/** Out or RPC cache exception code
 * @type string */
AjxException.OUT_OF_RPC_CACHE = "AjxException.OUT_OF_RPC_CACHE";

/** Unsupported operation code
 * @type string */
AjxException.UNSUPPORTED = "AjxException.UNSUPPORTED";

/** Unknown error exception code
 * @type string */
AjxException.UNKNOWN_ERROR = "AjxException.UNKNOWN_ERROR";

/** Operation cancelled exception code
 * @type string */
AjxException.CANCELED = "AjxException.CANCELED";
