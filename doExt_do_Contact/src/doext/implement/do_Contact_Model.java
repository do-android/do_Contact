package doext.implement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import core.DoServiceContainer;
import core.helper.DoJsonHelper;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;
import core.object.DoSingletonModule;
import doext.bean.DoContactBean;
import doext.define.do_Contact_IMethod;

/**
 * 自定义扩展SM组件Model实现，继承DoSingletonModule抽象类，并实现do_Contact_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_Contact_Model extends DoSingletonModule implements do_Contact_IMethod {

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String PHONE = "phone";
	public static final String EMAIL = "email";

	public do_Contact_Model() throws Exception {
		super();
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {

		if ("getDataById".equals(_methodName)) {
			getDataById(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		if ("getData".equals(_methodName)) {
			getData(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		if ("addData".equals(_methodName)) {
			addData(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		if ("updateData".equals(_methodName)) {
			updateData(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		if ("deleteData".equals(_methodName)) {
			deleteData(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}

		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	/**
	 * 根据联系人ID获取通讯录联系人信息；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void getDataById(JSONObject _dictParas, final DoIScriptEngine _scriptEngine, final String _callbackFuncName) throws Exception {
		String _id = DoJsonHelper.getString(_dictParas, "id", "");
		if (TextUtils.isEmpty(_id)) {
			throw new Exception("查询的id不能为空！");
		}
		DoContactBean _bean = getDataById(_id);
		DoInvokeResult _result = new DoInvokeResult(null);
		if (null != _bean) {
			_result.setResultNode(_bean.toJsonObject());
		} else {
			_result.setResultValue(JSONObject.NULL);
		}
		_scriptEngine.callback(_callbackFuncName, _result);
	}

	private DoContactBean getDataById(String _id) {

		Activity _context = DoServiceContainer.getPageViewFactory().getAppContext();
		ContentResolver _contentResolver = _context.getContentResolver();
		String[] _projection = { ContactsContract.Data.RAW_CONTACT_ID, ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1 };
		Cursor _cursor = _contentResolver.query(ContactsContract.Data.CONTENT_URI, _projection, ContactsContract.Data.RAW_CONTACT_ID + " = " + _id, null, null);

		DoContactBean _bean = null;
		if (_cursor != null && _cursor.getCount() > 0) {
			while (_cursor.moveToNext()) {
				String rawContactId = _cursor.getString(0);
				String mimetypeId = _cursor.getString(1);
				String data1 = _cursor.getString(2);
				if (null == _bean) {
					_bean = new DoContactBean();
					_bean.setPhones(new ArrayList<String>());
					_bean.setEmails(new ArrayList<String>());
					_bean.setId(rawContactId);
				}

				if (mimetypeId.contains("/name")) {
					_bean.setName(data1);
				} else if (mimetypeId.contains("/email")) {
					_bean.getEmails().add(data1);
				} else if (mimetypeId.contains("/phone")) {
					_bean.getPhones().add(data1);
				}
			}
			_cursor.close();
		}

		return _bean;
	}

	/**
	 * 获取通讯录联系人信息；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void getData(JSONObject _dictParas, final DoIScriptEngine _scriptEngine, final String _callbackFuncName) throws Exception {
		// 如果value 值为空，查询所有
		String _value = DoJsonHelper.getString(_dictParas, "value", "");
		JSONArray _paras = DoJsonHelper.getJSONArray(_dictParas, "types");
		// 查询的条件 ，支持 name，phone，email 模糊查询
		String _selection = null;
		String[] _selectionArray = null;
		if (!TextUtils.isEmpty(_value)) {
			if (null != _paras && _paras.length() > 0) {
				int _len = _paras.length();
				_selectionArray = new String[_len];
				for (int i = 0; i < _len; i++) {
					_selectionArray[i] = _paras.getString(i);
				}
			} else {
				_selectionArray = new String[] { NAME, PHONE, EMAIL };
			}
		}
		if (_selectionArray != null) {
			StringBuffer _tempStr = new StringBuffer();
			for (String _str : _selectionArray) {
				if (NAME.equals(_str)) {
					_tempStr.append(" || " + "(" + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE + "'" + " AND "
							+ ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME + " LIKE " + "'%" + _value + "%'" + ")");
				}
				if (PHONE.equals(_str)) {
					_tempStr.append(" || " + "(" + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'" + " AND "
							+ ContactsContract.CommonDataKinds.Phone.DATA + " LIKE " + "'%" + _value + "%'" + ")");
				}
				if (EMAIL.equals(_str)) {
					_tempStr.append(" || " + "(" + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE + "'" + " AND "
							+ ContactsContract.CommonDataKinds.Email.DATA + " LIKE " + "'%" + _value + "%'" + ")");
				}
			}
			_selection = _tempStr.substring(4).toString();
		}

		Activity _context = DoServiceContainer.getPageViewFactory().getAppContext();

		ContentResolver _contentResolver = _context.getContentResolver();
		// 查询的字段 raw_contact_id
		String[] _projection = { ContactsContract.Data.RAW_CONTACT_ID };
		Cursor _cursor = _contentResolver.query(ContactsContract.Data.CONTENT_URI, _projection, _selection, null, null);

		List<String> _ids = null;
		if (_cursor != null && _cursor.getCount() > 0) {
			_ids = new ArrayList<String>();
			while (_cursor.moveToNext()) {
				_ids.add(_cursor.getString(0));
			}
			_cursor.close();
		}

		if (null != _ids && _ids.size() > 0) {
			StringBuffer _idStr = new StringBuffer("( ");
			StringBuffer _tempStr = new StringBuffer();
			for (String _id : _ids) {
				_tempStr.append("," + _id);
			}
			_idStr.append(_tempStr.substring(1));
			_idStr.append(" )");
			_selection = ContactsContract.Data.RAW_CONTACT_ID + " IN " + _idStr.toString();
		}

		// 查询的字段 raw_contact_id, mimetype_id , data1
		_projection = new String[] { ContactsContract.Data.RAW_CONTACT_ID, ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1 };
		_cursor = _contentResolver.query(ContactsContract.Data.CONTENT_URI, _projection, _selection, null, null);

		Map<String, DoContactBean> datas = new HashMap<String, DoContactBean>();
		List<DoContactBean> contacts = new ArrayList<DoContactBean>();
		if (_cursor != null && _cursor.getCount() > 0) {
			while (_cursor.moveToNext()) {
				String rawContactId = _cursor.getString(0);
				String mimetypeId = _cursor.getString(1);
				String data1 = _cursor.getString(2);
				DoContactBean bean = datas.get(rawContactId);
				if (null == bean) {
					bean = new DoContactBean();
					bean.setPhones(new ArrayList<String>());
					bean.setEmails(new ArrayList<String>());
					bean.setId(rawContactId);
					datas.put(rawContactId, bean);
					contacts.add(bean);
				}

				if (mimetypeId.contains("/name")) {
					bean.setName(data1);
				} else if (mimetypeId.contains("/email")) {
					bean.getEmails().add(data1);
				} else if (mimetypeId.contains("/phone")) {
					bean.getPhones().add(data1);
				}
			}
			_cursor.close();
		}

		DoInvokeResult _result = new DoInvokeResult(null);
		JSONArray _array = new JSONArray();
		for (DoContactBean bean : contacts) {
			_array.put(bean.toJsonObject());
		}
		_result.setResultArray(_array);
		_scriptEngine.callback(_callbackFuncName, _result);

	}

	/**
	 * 添加联系人信息；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void addData(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		JSONArray _paras = DoJsonHelper.getJSONArray(_dictParas, "paras");
		Activity _context = DoServiceContainer.getPageViewFactory().getAppContext();
		List<DoContactBean> _beans = new ArrayList<DoContactBean>();
		JSONArray _array = new JSONArray();
		for (int i = 0; i < _paras.length(); i++) {
			JSONObject _obj = _paras.getJSONObject(i);
			DoContactBean _bean = new DoContactBean();
			if (_obj.has(NAME)) {
				_bean.setName(_obj.getString(NAME));
			}

			if (_obj.has(PHONE)) {
				_bean.setPhone(_obj.getString(PHONE));
			}

			if (_obj.has(EMAIL)) {
				_bean.setEmail(_obj.getString(EMAIL));
			}
			_beans.add(_bean);
			String _id = addContact(_context, _bean);
			_array.put(_id);
		}

		DoInvokeResult _result = new DoInvokeResult(null);
		_result.setResultArray(_array);
		_scriptEngine.callback(_callbackFuncName, _result);
	}

	// 添加联系人
	private String addContact(Context ctx, DoContactBean bean) {

		// 首先插入空值，再得到rawContactsId ，用于下面插值
		ContentValues values = new ContentValues();
		// insert a null value
		Uri rawContactUri = ctx.getContentResolver().insert(RawContacts.CONTENT_URI, values);
		long rawContactsId = ContentUris.parseId(rawContactUri);

		// 往刚才的空记录中插入姓名
		values.clear();
		values.put(StructuredName.RAW_CONTACT_ID, rawContactsId);
		values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
		values.put(StructuredName.DISPLAY_NAME, bean.getName());
		ctx.getContentResolver().insert(Data.CONTENT_URI, values);

		// 插入电话
		String phone = bean.getPhone();
		if (!TextUtils.isEmpty(phone)) {
			values.clear();
			values.put(Phone.RAW_CONTACT_ID, rawContactsId);
			values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
			values.put(Phone.NUMBER, phone);
			ctx.getContentResolver().insert(Data.CONTENT_URI, values);
		}

		// 插入邮箱
		String email = bean.getEmail();
		if (!TextUtils.isEmpty(email)) {
			values.clear();
			values.put(Phone.RAW_CONTACT_ID, rawContactsId);
			values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
			values.put(Email.ADDRESS, email);
			ctx.getContentResolver().insert(Data.CONTENT_URI, values);
		}

		return rawContactsId + "";

	}

	/**
	 * 删除联系人信息；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void deleteData(JSONObject _dictParas, final DoIScriptEngine _scriptEngine, final String _callbackFuncName) throws Exception {
		// 如果id 为空清空联系人列表
		JSONArray _ids = DoJsonHelper.getJSONArray(_dictParas, "ids");
		String _selection = null;
		if (null != _ids && _ids.length() > 0) {
			StringBuffer _idStr = new StringBuffer("( ");
			StringBuffer _tempStr = new StringBuffer();
			for (int i = 0; i < _ids.length(); i++) {
				String _id = _ids.getString(i);
				_tempStr.append("," + _id);
			}
			_idStr.append(_tempStr.substring(1));
			_idStr.append(" )");
			_selection = ContactsContract.RawContacts._ID + " IN  " + _idStr.toString();
		}

		Activity _context = DoServiceContainer.getPageViewFactory().getAppContext();
		ContentResolver _contentResolver = _context.getContentResolver();
		int _count = _contentResolver.delete(ContactsContract.RawContacts.CONTENT_URI, _selection, null);
		DoInvokeResult _result = new DoInvokeResult(null);
		_result.setResultBoolean(_count > 0);
		_scriptEngine.callback(_callbackFuncName, _result);
	}

	/**
	 * 修改联系人信息；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void updateData(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		String _id = DoJsonHelper.getString(_dictParas, "id", "");
		if (_id == null) {
			throw new Exception("id 参数值不能为空！");
		}

		JSONObject _paras = DoJsonHelper.getJSONObject(_dictParas, "paras");
		if (_paras == null) {
			throw new Exception("paras 参数值不能为空！");
		}

		// 获取当前id数据
		DoContactBean _bean = getDataById(_id);

		DoInvokeResult _result = new DoInvokeResult(null);
		if (_bean == null) {
			_result.setResultBoolean(false);
			_scriptEngine.callback(_callbackFuncName, _result);
			throw new Exception("联系人不存在");
		}

		Activity _context = DoServiceContainer.getPageViewFactory().getAppContext();
		String _name = _bean.getName();
		if (_paras.has(NAME)) {
			_name = _paras.getString(NAME);
		}

		ContentValues values = new ContentValues();
		// 更新姓名
		values.put(StructuredName.DISPLAY_NAME, _name);
		int _count = _context.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values, Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE + "=?",
				new String[] { _id, StructuredName.CONTENT_ITEM_TYPE });

		if (_paras.has(PHONE)) {
			String _phone = _paras.getString(PHONE);
			List<String> _phones = _bean.getPhones();
			if (null != _phones && _phones.size() > 0) { // 更新
				values.clear();
				values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, _phone);
				_count = _context.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values, Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE + "=?",
						new String[] { _id, Phone.CONTENT_ITEM_TYPE });
			} else { // 插入电话
				values.clear();
				values.put(Phone.RAW_CONTACT_ID, _id);
				values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
				values.put(Phone.NUMBER, _phone);
				_context.getContentResolver().insert(Data.CONTENT_URI, values);
			}
		}

		if (_paras.has(EMAIL)) {
			String _email = _paras.getString(EMAIL);
			List<String> _emails = _bean.getEmails();
			if (null != _emails && _emails.size() > 0) { // 更新
				values.clear();
				values.put(ContactsContract.CommonDataKinds.Email.ADDRESS, _email);
				_count = _context.getContentResolver().update(ContactsContract.Data.CONTENT_URI, values, Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE + "=?",
						new String[] { _id, Email.CONTENT_ITEM_TYPE });
			} else { // 插入电话
				values.clear();
				values.put(Phone.RAW_CONTACT_ID, _id);
				values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
				values.put(Email.ADDRESS, _email);
				_context.getContentResolver().insert(Data.CONTENT_URI, values);
			}
		}

		if (_count > 0) {
			_result.setResultBoolean(true);
		} else {
			_result.setResultBoolean(false);
		}
		_scriptEngine.callback(_callbackFuncName, _result);

	}

}
