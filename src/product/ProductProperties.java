package product;

import java.util.List;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Splitter;

@Component(value="productProperties")
public class ProductProperties implements InitializingBean, DisposableBean {

	@Value("#{appConfig['gsm.mcc']}")
	private short mcc;

	@Value("#{appConfig['gsm.name']}")
	private String gsm_name;

	@Value("#{appConfig['gsm.short_code']}")
	private short sc;

	private List<String> mnc;

	@Value("#{appConfig['msisdn.length']}")
	private byte msisdn_length;

	@Value("#{appConfig['voice.da']}")
	private short voice_da;

	@Value("#{appConfig['data.da']}")
	private short data_da;

	@Value("#{appConfig['customer.segment']}")
	private String customer_segment;

	private List<String> offer_id;

	private List<String> data_volume;

	private List<String> voice_volume;

	private List<String> Anumber_serviceClass_include_filter;
	private List<String> Anumber_db_include_filter;
	private List<String> Anumber_serviceClass_exclude_filter;
	private List<String> Anumber_db_exclude_filter;

	private List<String> Bnumber_serviceClass_include_filter;
	private List<String> Bnumber_db_include_filter;
	private List<String> Bnumber_serviceClass_exclude_filter;
	private List<String> Bnumber_db_exclude_filter;

	@Value("#{appConfig['gsm.mnc']}")
	public void setMnc(final String gsmmnc) {
		if(isSet(gsmmnc)) {
			mnc = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(gsmmnc);
		}
	 }

	@Value("#{appConfig['data.volume']}")
	public void setData_volume(final String data_volume) {
		if(isSet(data_volume)) {
			this.data_volume = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(data_volume);
		}
	}

	@Value("#{appConfig['voice.volume']}")
	public void setVoice_volume(final String voice_volume) {
		if(isSet(voice_volume)) {
			this.voice_volume = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(voice_volume);
		}
	}

	@Value("#{appConfig['offer.id']}")
	public void setOffer_id(final String offer_id) {
		if(isSet(offer_id)) {
			this.offer_id = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(offer_id);
		}
	}

	@Value("#{appConfig['Anumber.serviceClass.include_filter']}")
	public void setAnumber_serviceClass_include_filter(final String anumber_serviceClass_include_filter) {
		if(isSet(anumber_serviceClass_include_filter)) {
			Anumber_serviceClass_include_filter = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(anumber_serviceClass_include_filter);
		}
	}

	@Value("#{appConfig['Anumber.db.include_filter']}")
	public void setAnumber_db_include_filter(final String anumber_db_include_filter) {
		if(isSet(anumber_db_include_filter)) {
			Anumber_db_include_filter = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(anumber_db_include_filter);
		}
	}

	@Value("#{appConfig['Anumber.serviceClass.exclude_filter']}")
	public void setAnumber_serviceClass_exclude_filter(final String anumber_serviceClass_exclude_filter) {
		if(isSet(anumber_serviceClass_exclude_filter)) {
			Anumber_serviceClass_exclude_filter = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(anumber_serviceClass_exclude_filter);
		}
	}

	@Value("#{appConfig['Anumber.db.exclude_filter']}")
	public void setAnumber_db_exclude_filter(final String anumber_db_exclude_filter) {
		if(isSet(anumber_db_exclude_filter)) {
			Anumber_db_exclude_filter = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(anumber_db_exclude_filter);
		}
	}

	@Value("#{appConfig['Bnumber.serviceClass.include_filter']}")
	public void setBnumber_serviceClass_include_filter(final String bnumber_serviceClass_include_filter) {
		if(isSet(bnumber_serviceClass_include_filter)) {
			Bnumber_serviceClass_include_filter = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(bnumber_serviceClass_include_filter);
		}
	}

	@Value("#{appConfig['Bnumber.db.include_filter']}")
	public void setBnumber_db_include_filter(final String bnumber_db_include_filter) {
		if(isSet(bnumber_db_include_filter)) {
			Bnumber_db_include_filter = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(bnumber_db_include_filter);
		}
	}

	@Value("#{appConfig['Bnumber.serviceClass.exclude_filter']}")
	public void setBnumber_serviceClass_exclude_filter(final String bnumber_serviceClass_exclude_filter) {
		if(isSet(bnumber_serviceClass_exclude_filter)) {
			Bnumber_serviceClass_exclude_filter = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(bnumber_serviceClass_exclude_filter);
		}
	}

	@Value("#{appConfig['Bnumber.db.exclude_filter']}")
	public void setBnumber_db_exclude_filter(final String bnumber_db_exclude_filter) {
		if(isSet(bnumber_db_exclude_filter)) {
			Bnumber_db_exclude_filter = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(bnumber_db_exclude_filter);
		}
	}

	public short getMcc() {
		return mcc;
	}

	public String getGsm_name() {
		return gsm_name;
	}

	public short getSc() {
		return sc;
	}

	public List<String> getMnc() {
		return mnc;
	}

	public byte getMsisdn_length() {
		return msisdn_length;
	}

	public String getCustomer_segment() {
		return customer_segment;
	}

	public short getVoice_da() {
		return voice_da;
	}

	public short getData_da() {
		return data_da;
	}

	public List<String> getVoice_volume() {
		return voice_volume;
	}

	public List<String> getData_volume() {
		return data_volume;
	}

	public List<String> getOffer_id() {
		return offer_id;
	}

	public List<String> getAnumber_serviceClass_include_filter() {
		return Anumber_serviceClass_include_filter;
	}

	public List<String> getAnumber_db_include_filter() {
		return Anumber_db_include_filter;
	}

	public List<String> getAnumber_serviceClass_exclude_filter() {
		return Anumber_serviceClass_exclude_filter;
	}

	public List<String> getAnumber_db_exclude_filter() {
		return Anumber_db_exclude_filter;
	}

	public List<String> getBnumber_serviceClass_include_filter() {
		return Bnumber_serviceClass_include_filter;
	}

	public List<String> getBnumber_db_include_filter() {
		return Bnumber_db_include_filter;
	}

	public List<String> getBnumber_serviceClass_exclude_filter() {
		return Bnumber_serviceClass_exclude_filter;
	}

	public List<String> getBnumber_db_exclude_filter() {
		return Bnumber_db_exclude_filter;
	}

	public boolean isSet(String property_value) {
		if((property_value == null) || (property_value.trim().length() == 0)) return false;
		else return true;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
