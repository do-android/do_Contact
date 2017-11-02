package doext.define;

import org.json.JSONObject;

import core.interfaces.DoIScriptEngine;

/**
 * 声明自定义扩展组件方法
 */
public interface do_Contact_IMethod {
	void deleteData(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception;

	void getData(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception;

	void updateData(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception;

	void addData(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception;

	void getDataById(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception;
}