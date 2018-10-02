package product;

import java.util.List;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public interface ProductProperties extends InitializingBean, DisposableBean {

	public void setMnc(final String gsmmnc) ;

	public void setHappy_birthday_bonus_event_listeners(final String happy_birthday_bonus_event_listeners) ;

	public void setAir_hosts(final String air_hosts) ;

	public void setCustomer_segment_list(final String customer_segment_list) ;

	public void setData_volume(final String data_volume) ;

	public void setData_volume_rate(final String data_volume_rate) ;

	public void setVoice_volume(final String voice_volume) ;

	public void setVoice_volume_rate(final String voice_volume_rate) ;

	public void setOffer_id(final String offer_id) ;

	public void setAnumber_serviceClass_include_filter(final String anumber_serviceClass_include_filter) ;

	public void setAnumber_db_include_filter(final String anumber_db_include_filter) ;

	public void setAnumber_serviceClass_exclude_filter(final String anumber_serviceClass_exclude_filter) ;

	public void setAnumber_db_exclude_filter(final String anumber_db_exclude_filter) ;

	public void setBnumber_serviceClass_include_filter(final String bnumber_serviceClass_include_filter) ;

	public void setBnumber_db_include_filter(final String bnumber_db_include_filter) ;

	public void setBnumber_serviceClass_exclude_filter(final String bnumber_serviceClass_exclude_filter) ;

	public void setBnumber_db_exclude_filter(final String bnumber_db_exclude_filter) ;

	/*public void setBirth_dates_excluded(final String birth_dates_excluded) ;*/

	public short getMcc() ;

	public String getGsm_name() ;

	public short getSc() ;

	public String getSms_notifications_header() ;

	public boolean isHappy_birthday_bonus_event_listeners_activated() ;

	public List<String> getHappy_birthday_bonus_event_listeners() ;

	public List<String> getMnc() ;

	public byte getMsisdn_length() ;

	public List<String> getCustomer_segment_list() ;

	public String getCustomer_segment_filter() ;

	public int getAccumulator_id() ;

	public int getVoice_da() ;

	public int getData_da() ;

	public short getDa_expires_in_days() ;

	public List<String> getVoice_volume() ;

	public List<String> getVoice_volume_rate() ;

	public List<String> getData_volume() ;

	public List<String> getData_volume_rate() ;

	public List<String> getOffer_id() ;

	public String getMtnb_irm_database_subscriber_today_filter() ;

	public String getMtnb_irm_database_subscriber_many_days_filter() ;

	public List<String> getAnumber_serviceClass_include_filter() ;

	public List<String> getAnumber_db_include_filter() ;

	public List<String> getAnumber_serviceClass_exclude_filter() ;

	public List<String> getAnumber_db_exclude_filter() ;

	public List<String> getBnumber_serviceClass_include_filter() ;

	public List<String> getBnumber_db_include_filter() ;

	public List<String> getBnumber_serviceClass_exclude_filter() ;

	public List<String> getBnumber_db_exclude_filter() ;

	/*public List<String> getBirth_dates_excluded() ;*/

	public List<String> getAir_hosts() ;

	public int getAir_io_sleep() ;

	public int getAir_io_timeout() ;

	public int getAir_io_threshold() ;

	public String getAir_test_connection_msisdn() ;

	public byte getAir_preferred_host() ;

	public void setAir_preferred_host(byte air_preferred_host) ;

}
