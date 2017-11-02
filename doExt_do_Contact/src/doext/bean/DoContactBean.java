package doext.bean;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DoContactBean {

	private String id;
	private String name;
	private List<String> phones;
	private String phone;
	private List<String> emails;
	private String email;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getPhones() {
		return phones;
	}

	public void setPhones(List<String> phones) {
		this.phones = phones;
	}

	public List<String> getEmails() {
		return emails;
	}

	public void setEmails(List<String> emails) {
		this.emails = emails;
	}

	public String getPhone() {
		return phone;
	}

	public JSONArray listToJSONArray(List<String> _datas) {
		JSONArray _array = new JSONArray();
		for (String data : _datas) {
			_array.put(data);
		}
		return _array;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public JSONObject toJsonObject() throws JSONException {
		JSONObject _obj = new JSONObject();
		_obj.put("id", id);
		_obj.put("name", name);
		_obj.put("phone", listToJSONArray(phones));
		_obj.put("email", listToJSONArray(emails));
		return _obj;
	}

}
