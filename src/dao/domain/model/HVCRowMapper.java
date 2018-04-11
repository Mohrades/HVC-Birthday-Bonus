package dao.domain.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class HVCRowMapper implements RowMapper<HVC> {

	@Override
	public HVC mapRow(ResultSet rs, int rowNum) throws SQLException {
		// TODO Auto-generated method stub

		HVC hvc = new HVC(rs.getInt("ID"), rs.getString("MSISDN"), rs.getString("NAME"), rs.getInt("SEGMENT"), rs.getInt("LANGUAGE"), rs.getDate(",BIRTH_DATE"));
		hvc.setBonus(rs.getInt("BONUS"));
		hvc.setLast_update_time(rs.getTimestamp("LAST_UPDATE_TIME"));
		hvc.setBonus_expires_in(rs.getTimestamp("BONUS_EXPIRES_IN"));

		return hvc;
	}

}