package product;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import com.google.common.base.Splitter;

/*@Component(value="productProperties")*/
public class ProductPropertiesBasedOnPropertiesFactoryBean implements ProductProperties {

	@Value("#{appConfig['gsm.mcc']}")
	private short mcc;

	@Value("#{appConfig['gsm.name']}")
	private String gsm_name;

	@Value("#{appConfig['gsm.short_code']}")
	private short sc;

	@Value("#{appConfig['sms.notifications.header']}")
	private String sms_notifications_header;

	private List<String> mnc;

	@Value("#{appConfig['msisdn.length']}")
	private byte msisdn_length;

	@Value("#{appConfig['voice.da']}")
	private int voice_da;

	@Value("#{appConfig['data.da']}")
	private int data_da;

	@Value("#{appConfig['accumulator.id']}")
	private int accumulator_id;

	private List<String> customer_segment_list;

	@Value("#{appConfig['customer.segment.filter']}")
	private String customer_segment_filter;

	/*private List<String> birth_dates_excluded;*/

	private List<String> offer_id;

	private List<String> data_volume;
	private List<String> voice_volume;
	private List<String> data_volume_rate;
	private List<String> voice_volume_rate;

	private List<String> Anumber_serviceClass_include_filter;
	private List<String> Anumber_db_include_filter;
	private List<String> Anumber_serviceClass_exclude_filter;
	private List<String> Anumber_db_exclude_filter;

	private List<String> Bnumber_serviceClass_include_filter;
	private List<String> Bnumber_db_include_filter;
	private List<String> Bnumber_serviceClass_exclude_filter;
	private List<String> Bnumber_db_exclude_filter;

	private List<String> air_hosts;
	@Value("#{appConfig['air.io.sleep']}")
	private int air_io_sleep;
	@Value("#{appConfig['air.io.timeout']}")
	private int air_io_timeout;
	@Value("#{appConfig['air.io.threshold']}")
	private int air_io_threshold;
	@Value("#{appConfig['air.test.connection.msisdn']}")
	private String air_test_connection_msisdn;
	@Value("#{appConfig['air.preferred.host']}")
	private byte air_preferred_host;

	@Value("#{appConfig['happy.birthday.bonus.event.listeners.activated']}")
	private boolean happy_birthday_bonus_event_listeners_activated;

	private List<String> happy_birthday_bonus_event_listeners;

	@Value("#{appConfig['mtnb.irm.database.subscriber.today.filter']}")
	private String mtnb_irm_database_subscriber_today_filter;

	@Value("#{appConfig['mtnb.irm.database.subscriber.many.days.filter']}")
	private String mtnb_irm_database_subscriber_many_days_filter;

	@Value("#{appConfig['gsm.mnc']}")
	public void setMnc(final String gsmmnc) {
		if(isSet(gsmmnc)) {
			mnc = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(gsmmnc);
		}
	 }

	@Value("#{appConfig['happy.birthday.bonus.event.listeners']}")
	public void setHappy_birthday_bonus_event_listeners(final String happy_birthday_bonus_event_listeners) {
		if(isSet(happy_birthday_bonus_event_listeners)) {
			this.happy_birthday_bonus_event_listeners = Splitter.onPattern("[<]").trimResults().omitEmptyStrings().splitToList(happy_birthday_bonus_event_listeners);
		}
	}

	@Value("#{appConfig['air.hosts']}")
	public void setAir_hosts(final String air_hosts) {
		if(isSet(air_hosts)) {
			this.air_hosts = Splitter.onPattern("[;]").trimResults().omitEmptyStrings().splitToList(air_hosts);
		}
	}

	@Value("#{appConfig['customer.segment.list']}")
	public void setCustomer_segment_list(final String customer_segment_list) {
		if(isSet(customer_segment_list)) {
			this.customer_segment_list = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(customer_segment_list);
		}
	}

	@Value("#{appConfig['data.volume']}")
	public void setData_volume(final String data_volume) {
		if(isSet(data_volume)) {
			this.data_volume = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(data_volume);
		}
	}

	@Value("#{appConfig['data.volume.rate']}")
	public void setData_volume_rate(final String data_volume_rate) {
		if(isSet(data_volume_rate)) {
			this.data_volume_rate = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(data_volume_rate);
		}
	}

	@Value("#{appConfig['voice.volume']}")
	public void setVoice_volume(final String voice_volume) {
		if(isSet(voice_volume)) {
			this.voice_volume = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(voice_volume);
		}
	}

	@Value("#{appConfig['voice.volume.rate']}")
	public void setVoice_volume_rate(final String voice_volume_rate) {
		if(isSet(voice_volume_rate)) {
			this.voice_volume_rate = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(voice_volume_rate);
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

	/*@Value("#{appConfig['birth.dates.excluded']}")
	public void setBirth_dates_excluded(final String birth_dates_excluded) {
		if(isSet(birth_dates_excluded)) {
			this.birth_dates_excluded = Splitter.onPattern("[,]").trimResults().omitEmptyStrings().splitToList(birth_dates_excluded);
		}
	}*/

	public short getMcc() {
		return mcc;
	}

	public String getGsm_name() {
		return gsm_name;
	}

	public short getSc() {
		return sc;
	}

	public String getSms_notifications_header() {
		return sms_notifications_header;
	}

	public boolean isHappy_birthday_bonus_event_listeners_activated() {
		return happy_birthday_bonus_event_listeners_activated;
	}

	public List<String> getHappy_birthday_bonus_event_listeners() {
		return happy_birthday_bonus_event_listeners;
	}

	public List<String> getMnc() {
		return mnc;
	}

	public byte getMsisdn_length() {
		return msisdn_length;
	}

	public List<String> getCustomer_segment_list() {
		return customer_segment_list;
	}

	public String getCustomer_segment_filter() {
		return customer_segment_filter;
	}

	public int getAccumulator_id() {
		return accumulator_id;
	}

	public int getVoice_da() {
		return voice_da;
	}

	public int getData_da() {
		return data_da;
	}

	public List<String> getVoice_volume() {
		return voice_volume;
	}

	public List<String> getVoice_volume_rate() {
		return voice_volume_rate;
	}

	public List<String> getData_volume() {
		return data_volume;
	}

	public List<String> getData_volume_rate() {
		return data_volume_rate;
	}

	public List<String> getOffer_id() {
		return offer_id;
	}

	public String getMtnb_irm_database_subscriber_today_filter() {
		return mtnb_irm_database_subscriber_today_filter;
	}

	public String getMtnb_irm_database_subscriber_many_days_filter() {
		return mtnb_irm_database_subscriber_many_days_filter;
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

	/*public List<String> getBirth_dates_excluded() {
		return birth_dates_excluded;
	}*/

	public List<String> getAir_hosts() {
		return air_hosts;
	}

	public int getAir_io_sleep() {
		return air_io_sleep;
	}

	public int getAir_io_timeout() {
		return air_io_timeout;
	}

	public int getAir_io_threshold() {
		return air_io_threshold;
	}

	public String getAir_test_connection_msisdn() {
		return air_test_connection_msisdn;
	}

	public byte getAir_preferred_host() {
		return air_preferred_host;
	}

	public void setAir_preferred_host(byte air_preferred_host) {
		this.air_preferred_host = air_preferred_host;
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
