package dotest.module.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.os.Bundle;
import android.view.View;
import core.DoServiceContainer;
import doext.implement.do_Contact_Model;
import dotest.module.frame.debug.DoService;

public class DoContactTestActivity extends DoTestActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void initModuleModel() throws Exception {
		this.model = new do_Contact_Model();
	}

	@Override
	protected void initUIView() throws Exception {

	}

	@Override
	public void doTestProperties(View view) {
	}

	@Override
	protected void doTestSyncMethod() {

	}

	@Override
	protected void doTestAsyncMethod() {
		Map<String, Object>  _paras_loadString = new HashMap<String, Object>();
		JSONObject paras = new JSONObject();
		try {
			paras.put("id", "4");
			paras.put("name", "毛锐");
			paras.put("phone", "13922864549");
		} catch (Exception e) {
			e.printStackTrace();
		}
        _paras_loadString.put("paras", paras);
        DoService.asyncMethod(this.model, "getData", _paras_loadString, new DoService.EventCallBack() {
			@Override
			public void eventCallBack(String _data) {//回调函数
				DoServiceContainer.getLogEngine().writeDebug("异步方法回调：" + _data);
			}
		});
	}

	@Override
	protected void onEvent() {
	}

	@Override
	public void doTestFireEvent(View view) {
	}

}
