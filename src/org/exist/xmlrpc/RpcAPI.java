
/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001,  Wolfgang M. Meier (meier@ifs.tu-darmstadt.de)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 *  $Id$
 */
package org.exist.xmlrpc;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.io.IOException;

import org.exist.EXistException;
import org.exist.security.PermissionDeniedException;
import org.exist.security.User;
import org.xml.sax.SAXException;

/**
 *  Defines the methods callable through the XMLRPC interface.
 *
 *@author     Wolfgang Meier <meier@ifs.tu-darmstadt.de>
 *@created    21. Juni 2002
 */
public interface RpcAPI {

	public final static String SORT_EXPR = "sort-expr";
	public final static String NAMESPACES = "namespaces";
	public final static String VARIABLES = "variables";
	public final static String BASE_URI = "base-uri";
	
	/**
	 * Shut down the database.
	 * 
	 * @return boolean
	 */
	public boolean shutdown(User user) throws PermissionDeniedException;

	public boolean sync(User user);

	/**
	 *  Retrieve document by name. XML content is indented if prettyPrint is set
	 *  to >=0. Use supplied encoding for output. 
	 * 
	 *  This method is provided to retrieve a document with encodings other than UTF-8. Since the data is
	 *  handled as binary data, character encodings are preserved. byte[]-values
	 *  are automatically BASE64-encoded by the XMLRPC library.
	 *
	 *@param  name                           the document's name.
	 *@param  prettyPrint                    pretty print XML if >0.
	 *@param  encoding                       character encoding to use.
	 *@param  user
	 *@return   Document data as binary array. 
	 *@deprecated Use {@link #getDocument(User, String, Hashtable)} instead.
	 */
	byte[] getDocument(User user, String name, String encoding, int prettyPrint)
		throws EXistException, PermissionDeniedException;

	/**
	 *  Retrieve document by name. XML content is indented if prettyPrint is set
	 *  to >=0. Use supplied encoding for output and apply the specified stylesheet. 
	 * 
	 *  This method is provided to retrieve a document with encodings other than UTF-8. Since the data is
	 *  handled as binary data, character encodings are preserved. byte[]-values
	 *  are automatically BASE64-encoded by the XMLRPC library.
	 *
	 *@param  name                           the document's name.
	 *@param  prettyPrint                    pretty print XML if >0.
	 *@param  encoding                       character encoding to use.
	 *@param  user                           Description of the Parameter
	 *@return                                The document value
	 *@deprecated Use {@link #getDocument(User, String, Hashtable)} instead.
	 */
	byte[] getDocument(User user, String name, String encoding, int prettyPrint, String stylesheet)
		throws EXistException, PermissionDeniedException;
	
     /**
	 * Retrieve document by name.  All optional output parameters are passed as key/value pairs
	 * int the hashtable <code>parameters</code>.
	 * 
	 * Valid keys may either be taken from {@link javax.xml.transform.OutputKeys} or 
	 * {@link org.exist.storage.serializers.EXistOutputKeys}. For example, the encoding is identified by
	 * the value of key {@link javax.xml.transform.OutputKeys#ENCODING}.
	 *
	 *@param  name                           the document's name.
	 *@param  parameters                      Hashtable of parameters.
	 *@return                                The document value
	 */		
	byte[] getDocument(User user, String name, Hashtable parameters)
			throws EXistException, PermissionDeniedException;	
		

	String getDocumentAsString(User user, String name, int prettyPrint)
			throws EXistException, PermissionDeniedException;
			
	String getDocumentAsString(User user, String name, int prettyPrint, String stylesheet)
		throws EXistException, PermissionDeniedException;
	
	String getDocumentAsString(User user, String name, Hashtable parameters)
		throws EXistException, PermissionDeniedException;
	
	byte[] getBinaryResource(User user, String name)
		throws EXistException, PermissionDeniedException;
	
	/**
	 *  Does the document identified by <code>name</code> exist in the
	 *  repository?
	 *
	 *@param  name                           Description of the Parameter
	 *@param  user                           Description of the Parameter
	 *@return                                Description of the Return Value
	 *@exception  EXistException             Description of the Exception
	 *@exception  PermissionDeniedException  Description of the Exception
	 */
	boolean hasDocument(User user, String name) throws EXistException, PermissionDeniedException;

	/**
	 *  Get a list of all documents contained in the database.
	 *
	 *@param  user
	 *@return  list of document paths
	 *@exception  EXistException             Description of the Exception
	 *@exception  PermissionDeniedException  Description of the Exception
	 */
	Vector getDocumentListing(User user) throws EXistException, PermissionDeniedException;

	/**
	 *  Get a list of all documents contained in the collection.
	 *
	 *@param  collection                     the collection to use.
	 *@param  user                           Description of the Parameter
	 *@return                                list of document paths
	 *@exception  EXistException             Description of the Exception
	 *@exception  PermissionDeniedException  Description of the Exception
	 */
	Vector getDocumentListing(User user, String collection)
		throws EXistException, PermissionDeniedException;

	Hashtable listDocumentPermissions(User user, String name)
		throws EXistException, PermissionDeniedException;

	Hashtable listCollectionPermissions(User user, String name)
		throws EXistException, PermissionDeniedException;

	/**
	 *  Describe a collection: returns a struct with the  following fields:
	 *  
	 * <pre>
	 *	name				The name of the collection
	 *	
	 *	owner				The name of the user owning the collection.
	 *	
	 *	group				The group owning the collection.
	 *	
	 *	permissions	The permissions that apply to this collection (int value)
	 *	
	 *	created			The creation date of this collection (long value)
	 *	
	 *	collections		An array containing the names of all subcollections.
	 *	
	 *	documents		An array containing a struct for each document in the collection.
	 *	</pre>
	 *
	 *	Each of the elements in the "documents" array is another struct containing the properties
	 *	of the document:
	 *
	 *	<pre>
	 *	name				The full path of the document.
	 *	
	 *	owner				The name of the user owning the document.
	 *	
	 *	group				The group owning the document.
	 *	
	 *	permissions	The permissions that apply to this document (int)
	 *	
	 *	type					Type of the resource: either "XMLResource" or "BinaryResource"
	 *	</pre>
	 *
	 *@param  rootCollection                 Description of the Parameter
	 *@param  user                           Description of the Parameter
	 *@return                                The collectionDesc value
	 *@exception  EXistException             Description of the Exception
	 *@exception  PermissionDeniedException  Description of the Exception
	 */
	Hashtable getCollectionDesc(User user, String rootCollection)
		throws EXistException, PermissionDeniedException;

	Hashtable describeCollection(User user, String collectionName)
		throws EXistException, PermissionDeniedException;
	
	Hashtable describeResource(User user, String resourceName)
		throws EXistException, PermissionDeniedException;
	
	/**
	 * Returns the number of resources in the collection identified by
	 * collectionName.
	 * 
	 * @param user
	 * @param collection
	 * @return
	 * @throws EXistException
	 * @throws PermissionDeniedException
	 */
	int getResourceCount(User user, String collectionName)
		throws EXistException, PermissionDeniedException;
	
	/**
	 *  Retrieve a single node from a document. The node is identified by it's
	 *  internal id.
	 *
	 *@param  doc                            the document containing the node
	 *@param  id                             the node's internal id
	 *@param  user                           Description of the Parameter
	 *@return                                Description of the Return Value
	 *@exception  EXistException             Description of the Exception
	 *@exception  PermissionDeniedException  Description of the Exception
	 */
	byte[] retrieve(User user, String doc, String id)
		throws EXistException, PermissionDeniedException;

	/**
	 *  Retrieve a single node from a document. The node is identified by it's
	 *  internal id.
	 *
	 *@param  doc                            the document containing the node
	 *@param  id                             the node's internal id
	 *@param  prettyPrint                    result is pretty printed if >0
	 *@param  encoding                       character encoding to use
	 *@param  user                           Description of the Parameter
	 *@return                                Description of the Return Value
	 *@exception  EXistException             Description of the Exception
	 *@exception  PermissionDeniedException  Description of the Exception
	 */
	byte[] retrieve(User user, String doc, String id, Hashtable parameters)
		throws EXistException, PermissionDeniedException;

	String retrieveAsString(User user, String doc, String id, Hashtable parameters)
		throws EXistException, PermissionDeniedException;

	public byte[] retrieveAll(User user, int resultId, Hashtable parameters) 
	throws EXistException, PermissionDeniedException;
	
	Hashtable queryP(User user, byte[] xpath, Hashtable parameters)
		throws EXistException, PermissionDeniedException;

	Hashtable queryP(User user, byte[] xpath, String docName, String s_id, Hashtable parameters)
		throws EXistException, PermissionDeniedException;
	
	/**
	 *  execute XPath query and return howmany nodes from the result set,
	 *  starting at position <code>start</code>. If <code>prettyPrint</code> is
	 *  set to >0 (true), results are pretty printed.
	 *
	 *@param  xpath                          the XPath query to execute
	 *@param  howmany                        maximum number of results to
	 *      return.
	 *@param  start                          item in the result set to start
	 *      with.
	 *@param  prettyPrint                    turn on pretty printing if >0.
	 *@param  encoding                       the character encoding to use.
	 *@param  sortExpr                       Description of the Parameter
	 *@param  user                           Description of the Parameter
	 *@return                                Description of the Return Value
	 *@exception  EXistException             Description of the Exception
	 *@exception  PermissionDeniedException  Description of the Exception
	 *@depreceated                           use Vector query() or int
	 *      executeQuery() instead
	 */
	String query(
		User user,
		String xquery,
		int howmany,
		int start,
		Hashtable parameters)
		throws EXistException, PermissionDeniedException;

	/**
	 *  execute XPath query and return a summary of hits per document and hits
	 *  per doctype. This method returns a struct with the following fields:
	 *
	 *  <table border="1">
	 *
	 *    <tr>
	 *
	 *      <td>
	 *        "queryTime"
	 *      </td>
	 *
	 *      <td>
	 *        int
	 *      </td>
	 *
	 *    </tr>
	 *
	 *    <tr>
	 *
	 *      <td>
	 *        "hits"
	 *      </td>
	 *
	 *      <td>
	 *        int
	 *      </td>
	 *
	 *    </tr>
	 *
	 *    <tr>
	 *
	 *      <td>
	 *        "documents"
	 *      </td>
	 *
	 *      <td>
	 *        array of array: Object[][3]
	 *      </td>
	 *
	 *    </tr>
	 *
	 *    <tr>
	 *
	 *      <td>
	 *        "doctypes"
	 *      </td>
	 *
	 *      <td>
	 *        array of array: Object[][2]
	 *      </td>
	 *
	 *    </tr>
	 *
	 *  </table>
	 *  Documents and doctypes represent tables where each row describes one
	 *  document or doctype for which hits were found. Each document entry has
	 *  the following structure: docId (int), docName (string), hits (int) The
	 *  doctype entry has this structure: doctypeName (string), hits (int)
	 *
	 *@param  xpath                          Description of the Parameter
	 *@param  user                           Description of the Parameter
	 *@return                                Description of the Return Value
	 *@exception  EXistException             Description of the Exception
	 *@exception  PermissionDeniedException  Description of the Exception
	 *@depreceated                           use Vector query() or int
	 *      executeQuery() instead
	 */
	Hashtable querySummary(User user, String xquery)
		throws EXistException, PermissionDeniedException;

	String createResourceId(User user, String collection)
		throws EXistException, PermissionDeniedException;
	
	/**
	 *  Parse an XML document and store it into the database. The document will
	 *  later be identified by <code>docName</code>. Some xmlrpc clients seem to
	 *  have problems with character encodings when sending xml content. To
	 *  avoid this, parse() accepts the xml document content as byte[]. If
	 *  <code>overwrite</code> is >0, an existing document with the same name
	 *  will be replaced by the new document.
	 *
	 *@param  xmlData                        The document data
	 *@param  docName                      The path where the document will be stored 
	 *@return                                		
	 *@exception  EXistException
	 *@exception  PermissionDeniedException
	 */
	boolean parse(User user, byte[] xmlData, String docName)
		throws EXistException, PermissionDeniedException;

	/**
	 *  Parse an XML document and store it into the database. The document will
	 *  later be identified by <code>docName</code>. Some xmlrpc clients seem to
	 *  have problems with character encodings when sending xml content. To
	 *  avoid this, parse() accepts the xml document content as byte[]. If
	 *  <code>overwrite</code> is >0, an existing document with the same name
	 *  will be replaced by the new document.
	 *
	 *@param  xmlData                        The document data
	 *@param  docName                      The path where the document will be stored 
	 *@param  overwrite                      Overwrite an existing document with the same path?
	 *@return                                		
	 *@exception  EXistException
	 *@exception  PermissionDeniedException
	 */
	boolean parse(User user, byte[] xmlData, String docName, int overwrite)
	throws EXistException, PermissionDeniedException;

	boolean parse(User user, String xml, String docName, int overwrite)
		throws EXistException, PermissionDeniedException;

	boolean parse(User user, String xml, String docName)
		throws EXistException, PermissionDeniedException;

	/**
	 * An alternative to parse() for larger XML documents. The document
	 * is first uploaded chunk by chunk using upload(), then parseLocal() is
	 * called to actually store the uploaded file.
	 * 
	 * @param user
	 * @param chunk the current chunk
	 * @param length total length of the file 
	 * @return the name of the file to which the chunk has been appended.
	 * @throws EXistException
	 * @throws PermissionDeniedException
	 */
	String upload(User user, byte[] chunk, int length)
		throws EXistException, PermissionDeniedException;

	/**
	 * An alternative to parse() for larger XML documents. The document
	 * is first uploaded chunk by chunk using upload(), then parseLocal() is
	 * called to actually store the uploaded file.
	 * 
	 * @param user
	 * @param chunk the current chunk
	 * @param file the name of the file to which the chunk will be appended. This
	 * should be the file name returned by the first call to upload.
	 * @param length total length of the file 
	 * @return the name of the file to which the chunk has been appended.
	 * @throws EXistException
	 * @throws PermissionDeniedException
	 */
	String upload(User user, String file, byte[] chunk, int length)
		throws EXistException, PermissionDeniedException;

	/**
	 * Parse a file previously uploaded with upload.
	 * 
	 * The temporary file will be removed.
	 * 
	 * @param user
	 * @param localFile
	 * @throws EXistException
	 * @throws IOException
	 */
	public boolean parseLocal(User user, String localFile, String docName, boolean replace)
		throws EXistException, PermissionDeniedException, SAXException;

	/**
	 * Store data as a binary resource.
	 * 
	 * @param user
	 * @param data the data to be stored
	 * @param docName the path to the new document
	 * @param replace if true, an old document with the same path will be overwritten
	 * @return
	 * @throws EXistException
	 * @throws PermissionDeniedException
	 */
	public boolean storeBinary(User user, byte[] data, String docName, boolean replace)
	throws EXistException, PermissionDeniedException;
	
	/**
	 *  Remove a document from the database.
	 *
	 *@param  docName path to the document to be removed
	 *@param  user                           
	 *@return                                true on success.
	 *@exception  EXistException             
	 *@exception  PermissionDeniedException  
	 */
	boolean remove(User user, String docName) throws EXistException, PermissionDeniedException;

	/**
	 *  Remove an entire collection from the database.
	 *
	 *@param  name path to the collection to be removed.
	 *@param  user
	 *@return
	 *@exception  EXistException             
	 *@exception  PermissionDeniedException 
	 */
	boolean removeCollection(User user, String name)
		throws EXistException, PermissionDeniedException;

	/** 
	 * Create a new collection on the database.
	 * 
	 * @param user
	 * @param name the path to the new collection.
	 * @return
	 * @throws EXistException
	 * @throws PermissionDeniedException
	 */
	boolean createCollection(User user, String name)
		throws EXistException, PermissionDeniedException;

	/**
	 *  Execute XPath query and return a reference to the result set. The
	 *  returned reference may be used later to get a summary of results or
	 *  retrieve the actual hits.
	 *
	 *@param  xpath                          Description of the Parameter
	 *@param  encoding                       Description of the Parameter
	 *@param  user                           Description of the Parameter
	 *@return                                Description of the Return Value
	 *@exception  EXistException             Description of the Exception
	 *@exception  PermissionDeniedException  Description of the Exception
	 */
	int executeQuery(User user, byte[] xpath, String encoding, Hashtable parameters)
		throws EXistException, PermissionDeniedException;

	int executeQuery(User user, byte[] xpath, Hashtable parameters) throws EXistException, PermissionDeniedException;

	int executeQuery(User user, String xpath, Hashtable parameters) throws EXistException, PermissionDeniedException;

	/**
	 *  Retrieve a summary of the result set identified by it's result-set-id.
	 *  This method returns a struct with the following fields:
	 *
	 *  <tableborder="1">
	 *
	 *    <tr>
	 *
	 *      <td>
	 *        "queryTime"
	 *      </td>
	 *
	 *      <td>
	 *        int
	 *      </td>
	 *
	 *    </tr>
	 *
	 *    <tr>
	 *
	 *      <td>
	 *        "hits"
	 *      </td>
	 *
	 *      <td>
	 *        int
	 *      </td>
	 *
	 *    </tr>
	 *
	 *    <tr>
	 *
	 *      <td>
	 *        "documents"
	 *      </td>
	 *
	 *      <td>
	 *        array of array: Object[][3]
	 *      </td>
	 *
	 *    </tr>
	 *
	 *    <tr>
	 *
	 *      <td>
	 *        "doctypes"
	 *      </td>
	 *
	 *      <td>
	 *        array of array: Object[][2]
	 *      </td>
	 *
	 *    </tr>
	 *
	 *  </table>
	 *  Documents and doctypes represent tables where each row describes one
	 *  document or doctype for which hits were found. Each document entry has
	 *  the following structure: docId (int), docName (string), hits (int) The
	 *  doctype entry has this structure: doctypeName (string), hits (int)
	 *
	 *@param  resultId                       Description of the Parameter
	 *@param  user                           Description of the Parameter
	 *@return                                Description of the Return Value
	 *@exception  EXistException             Description of the Exception
	 *@exception  PermissionDeniedException  Description of the Exception
	 */
	Hashtable querySummary(User user, int resultId)
		throws EXistException, PermissionDeniedException;

	Hashtable getPermissions(User user, String resource)
		throws EXistException, PermissionDeniedException;

	/**
	 *  Get the number of hits in the result set identified by it's
	 *  result-set-id.
	 *
	 *@param  resultId                       Description of the Parameter
	 *@param  user                           Description of the Parameter
	 *@return                                The hits value
	 *@exception  EXistException             Description of the Exception
	 *@exception  PermissionDeniedException  Description of the Exception
	 */
	int getHits(User user, int resultId) throws EXistException, PermissionDeniedException;

	/**
	 *  Retrieve a single result from the result-set identified by resultId. The
	 *  XML fragment at position num in the result set is returned.
	 *
	 *@param  resultId                       Description of the Parameter
	 *@param  num                            Description of the Parameter
	 *@param  prettyPrint                    Description of the Parameter
	 *@param  encoding                       Description of the Parameter
	 *@param  user                           Description of the Parameter
	 *@return                                Description of the Return Value
	 *@exception  EXistException             Description of the Exception
	 *@exception  PermissionDeniedException  Description of the Exception
	 */
	byte[] retrieve(User user, int resultId, int num, Hashtable parameters)
		throws EXistException, PermissionDeniedException;

	boolean setUser(User user, String name, String passwd, Vector groups, String home)
		throws EXistException, PermissionDeniedException;

	boolean setUser(User user, String name, String passwd, Vector groups)
		throws EXistException, PermissionDeniedException;

	boolean setPermissions(User user, String resource, String permissions)
		throws EXistException, PermissionDeniedException;

	boolean setPermissions(User user, String resource, int permissions)
		throws EXistException, PermissionDeniedException;

	boolean setPermissions(
		User user,
		String resource,
		String owner,
		String ownerGroup,
		String permissions)
		throws EXistException, PermissionDeniedException;

	boolean setPermissions(
		User user,
		String resource,
		String owner,
		String ownerGroup,
		int permissions)
		throws EXistException, PermissionDeniedException;

	public boolean lockResource(User user, String path, String userName) 
	throws EXistException, PermissionDeniedException;
	
	public boolean unlockResource(User user, String path) 
	throws EXistException, PermissionDeniedException;

	public String hasUserLock(User user, String path)
	throws EXistException, PermissionDeniedException;
	
	Hashtable getUser(User user, String name) throws EXistException, PermissionDeniedException;

	Vector getUsers(User user) throws EXistException, PermissionDeniedException;

	boolean removeUser(User user, String name) throws EXistException, PermissionDeniedException;

	Vector getGroups(User user) throws EXistException, PermissionDeniedException;

	Vector getIndexedElements(User user, String collectionName, boolean inclusive)
		throws EXistException, PermissionDeniedException;

	Vector scanIndexTerms(
		User user,
		String collectionName,
		String start,
		String end,
		boolean inclusive)
		throws PermissionDeniedException, EXistException;

	boolean releaseQueryResult(User user, int handle);

	int xupdate(User user, String collectionName, byte[] xupdate)
		throws PermissionDeniedException, EXistException, SAXException;
	
	int xupdateResource(User user, String resource, byte[] xupdate)
		throws PermissionDeniedException, EXistException, SAXException;

	int xupdateResource(User user, String resource, byte[] xupdate, String encoding)
		throws PermissionDeniedException, EXistException, SAXException;

		
	Date getCreationDate(User user, String collectionName)
		throws PermissionDeniedException, EXistException;
	
	Vector getTimestamps(User user, String documentName)
		throws PermissionDeniedException, EXistException;
		
	boolean copyCollection(User user, String name, String namedest)
	    throws PermissionDeniedException, EXistException;

	Vector getDocumentChunk(User user, String name, Hashtable parameters)
	throws EXistException, PermissionDeniedException, IOException;
	
	byte[] getDocumentChunk(User user, String name, int start, int stop)
	throws EXistException, PermissionDeniedException, IOException;
}
