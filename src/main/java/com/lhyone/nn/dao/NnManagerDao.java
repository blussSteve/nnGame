package com.lhyone.nn.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.lhyone.nn.entity.BugUser;
import com.lhyone.nn.entity.GoldRecord;
import com.lhyone.nn.entity.JoinRoomUser;
import com.lhyone.nn.entity.NnRoom;
import com.lhyone.nn.entity.NnRoomMatchUser;
import com.lhyone.nn.entity.NnRoomMultipleDic;
import com.lhyone.nn.entity.SysAgentIncomeConfig;
import com.lhyone.nn.entity.UserCostRecord;
import com.lhyone.nn.entity.UserMatchRecord;
import com.lhyone.nn.enums.GameTypeEnum;
import com.lhyone.nn.vo.DbVo;
import com.lhyone.nn.vo.PropDicVo;
import com.lhyone.util.DbUtil;
import com.xiaoleilu.hutool.date.DatePattern;
import com.xiaoleilu.hutool.date.DateUtil;

public class NnManagerDao {

	private static NnManagerDao _instance;

	public static NnManagerDao instance() {

		if (_instance == null) {

			return _instance = new NnManagerDao();

		}

		return _instance;
	}

	/**
	 * 获取翻倍规则
	 * 
	 * @return
	 */
	public List<NnRoomMultipleDic> queryNnRoomMultipleDic() {
		String sql = "SELECT * FROM nn_room_multiple_dic t WHERE t.status=1";
		List<NnRoomMultipleDic> list = DbUtil.getInstance().getList(NnRoomMultipleDic.class, sql, new Object[] {});
		return list;
	}

	/**
	 * 获取牛牛房间
	 * 
	 * @param roomId
	 * @return
	 */
	public NnRoom getNnRoom(String roomNo) {

		String sql = "SELECT * FROM nn_room t WHERE t.room_no=? AND t.room_status=1 AND t.room_type=1  ORDER BY t.create_date DESC LIMIT 1";
		return DbUtil.getInstance().getOne(NnRoom.class, sql, new Object[] { roomNo });
	}

	/**
	 * 检查用户是是否加入该房间
	 * 
	 * @param roomId
	 * @param userId
	 * @return
	 */
	public boolean checkUserIsJoinRoom(long roomId, long userId) {

		String sql = "SELECT COUNT(1) FROM u_join_room_user t WHERE t.room_id=? AND t.user_id=? AND t.game_type=1 LIMIT 1";
		long l = DbUtil.getInstance().getValue(sql, new Object[] { roomId, userId });
		return l > 0;
	}

	/**
	 * 获取用户当前金币数
	 * 
	 * @param userId
	 * @return
	 */
	public long getUserGold(long userId) {
		String sql = "SELECT t.cur_gold FROM u_user_account t WHERE t.user_id=?";
		long userGold = DbUtil.getInstance().getValue(sql, new Object[] { userId });

		return userGold;
	}

	/**
	 * 获取bug用户
	 * 
	 * @param userId
	 * @return
	 */
	public BugUser getBugUser(String uids) {
		String sql = "SELECT * FROM bug_user t WHERE t.user_id IN(" + uids + ") AND t.status=1 AND game_type=1 ORDER BY RAND() LIMIT 1";
		BugUser user = DbUtil.getInstance().getOne(BugUser.class, sql, null);

		return user;
	}

	/**
	 * 清楚房间编号
	 * 
	 * @param roomNo
	 * @return
	 */
	public boolean clearRoomNo(String roomNo) {

		int i = DbUtil.getInstance().executeUpdate("update u_room_no set enable=0,game_type=null where enable=1 and room_no=?", new Object[] { roomNo });

		if (i > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 批量更新俱乐部流水信息
	 * 
	 * @param list
	 * @return
	 */
	public boolean sendUserProp(long sendUserId, long beSendUserId, PropDicVo prop) {
		Connection conn = null;
		Statement stmt = null;
		try {

			conn = DbUtil.getInstance().getConnection();
			stmt = conn.createStatement();
			conn.setAutoCommit(false);

			String sql1 = "INSERT INTO `lhy_prop_record` (`user_id`,`be_user_id`, `prop_num`, `cost_prop_count`, `cost_gold_count`,`unit`,`create_date`) VALUES(" + sendUserId + "," + beSendUserId + "," + prop.getNum() + ",1,"
					+ prop.getCostGold() + ",'1','" + DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN) + "')";
			stmt.addBatch(sql1);
			String sql2 = "update u_user_account t set t.cur_gold=t.cur_gold-" + prop.getCostGold() + " , t.total_cost_gold=t.total_cost_gold+" + prop.getCostGold() + "where t.user_id=" + sendUserId;
			stmt.addBatch(sql2);
			int i = stmt.executeBatch().length;
			conn.commit();
			if (i > 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (null != conn) {
					conn.rollback();
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				if (stmt != null) {
					stmt.clearBatch();
					stmt.close();
				}
				if (null != conn) {
					conn.close();
				}
			} catch (SQLException e) {

				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 获取代理级别收益配置
	 * 
	 * @return
	 */
	public List<SysAgentIncomeConfig> querySysAgentIncomeConfig() {
		String sql = "SELECT * FROM sys_agent_income_config where game_type=1";

		List<SysAgentIncomeConfig> list = DbUtil.getInstance().getList(SysAgentIncomeConfig.class, sql, null);
		return list;
	}

	/**
	 * 
	 * @param userId
	 * @param gold
	 * @return
	 */
	public boolean subUserGold(long userId, int gold) {

		String sql = "UPDATE	u_user_account t SET t.cur_gold=t.cur_gold-? , t.total_cost_gold=t.total_cost_gold=? WHERE t.user_id=?";
		int i = DbUtil.getInstance().executeUpdate(sql, new Object[] { gold, gold, userId });

		if (i > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 增加db数据
	 * 
	 * @param list
	 * @return
	 */
	public boolean addDb(List<DbVo> list) {

		Connection conn = null;
		Statement stmt = null;
		try {

			conn = DbUtil.getInstance().getConnection();
			stmt = conn.createStatement();
			conn.setAutoCommit(false);

			for (DbVo bean : list) {

				String sql1 = "UPDATE u_user_account t SET t.cur_gold=t.cur_gold+" + bean.getWinGold() + " , t.total_cost_gold=t.total_cost_gold+" + bean.getWinGold() + " WHERE t.user_id=" + bean.getUserId() + "";
				stmt.addBatch(sql1);

				GoldRecord gr = bean.getGoldRecord();

				String sql2 = "insert into u_gold_record (user_id, order_no, gold_count, cost_type, bind_id, create_date) values(" + gr.getUserId() + ",'" + gr.getOrderNo() + "'," + gr.getGoldCount() + "," + gr.getCostType() + ","
						+ gr.getBindId() + ",'" + DateUtil.format(gr.getCreateDate(), DatePattern.NORM_DATETIME_PATTERN) + "')";
				stmt.addBatch(sql2);

				UserCostRecord ucr = bean.getUserCostRecord();

				String sql3 = "insert into u_user_cost_record (user_id, cost_gold, room_id, room_no, bind_id,order_no, game_type, is_del, create_date) values(" + ucr.getUserId() + "," + ucr.getCostGold() + "," + ucr.getRoomId() + ",'"
						+ ucr.getRoomNo() + "'," + ucr.getRoomId() + ",'" + ucr.getOrderNo() + "'," + ucr.getGameType() + "," + ucr.getIsDel() + ",'" + DateUtil.format(ucr.getCreateDate(), DatePattern.NORM_DATETIME_PATTERN) + "')";
				stmt.addBatch(sql3);

				NnRoomMatchUser nrmu = bean.getNnRoomMatchUser();
				String sql4 = "insert into nn_room_match_user (user_id, room_id, room_no, match_num, total_gold, base_gold, win_gold,cost_gold, player_role, cards, card_type, is_win, doublex,order_no, create_date,is_bug)" + " values("
						+ nrmu.getUserId() + "," + nrmu.getRoomId() + ",'" + nrmu.getRoomNo() + "'," + nrmu.getMatchNum() + "," + nrmu.getTotalGold() + "," + nrmu.getBaseGold() + "," + nrmu.getWinGold() + "," + nrmu.getCostGold() + ","
						+ nrmu.getPlayerRole() + ",'" + nrmu.getCards() + "'," + nrmu.getCardType() + "," + nrmu.getIsWin() + "," + nrmu.getDoublex() + ",'" + nrmu.getOrderNo() + "','"
						+ DateUtil.format(nrmu.getCreateDate(), DatePattern.NORM_DATETIME_PATTERN) + "'," + nrmu.getIsBug() + ")";

				stmt.addBatch(sql4);

				UserMatchRecord umr = bean.getUserMatchRecord();
				String sql5 = "insert into u_user_match_record (user_id, win_gold, game_type, room_id, bind_id, mark,order_no, create_date) values(" + umr.getUserId() + "," + umr.getWinGold() + "," + umr.getGameType() + ","
						+ umr.getRoomId() + "," + umr.getRoomId() + ",'" + umr.getMark() + "','" + umr.getOrderNo() + "','" + DateUtil.format(umr.getCreateDate(), DatePattern.NORM_DATETIME_PATTERN) + "')";

				stmt.addBatch(sql5);

				System.out.println(sql1);
				System.out.println(sql2);
				System.out.println(sql3);
				System.out.println(sql4);
				System.out.println(sql5);
			}

			int i = stmt.executeBatch().length;
			conn.commit();
			if (i > 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (null != conn) {
					conn.rollback();
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				if (stmt != null) {
					stmt.clearBatch();
					stmt.close();
				}
				if (null != conn) {
					conn.close();
				}
			} catch (SQLException e) {

				e.printStackTrace();
			}
		}
		return false;

	}

	/**
	 * 剔除房间用户
	 * 
	 * @param userId
	 * @param roomNo
	 * @param gameType
	 * @return
	 */
	public boolean deleteRoomUser(long userId, String roomNo, int gameType) {
		String sql = "DELETE FROM u_join_room_user WHERE user_id=? AND room_no=? AND game_type=?";
		int i = DbUtil.getInstance().executeUpdate(sql, new Object[] { userId, roomNo, gameType });
		if (i > 0) {
			return true;
		}
		return false;

	}

	/**
	 * 剔除房间用户
	 * 
	 * @param userId
	 * @param roomNo
	 * @param gameType
	 * @return
	 */
	public boolean deleteRoomUser(String roomNo, int gameType) {
		String sql = "DELETE FROM u_join_room_user WHERE room_no=? AND game_type=?";
		int i = DbUtil.getInstance().executeUpdate(sql, new Object[] { roomNo, gameType });
		if (i > 0) {
			return true;
		}
		return false;

	}

	/**
	 * 增加牛牛房间用户
	 * 
	 * @param roomUser
	 * @return
	 */
	public int addNnRoomUser(JoinRoomUser roomUser) {
		String sql = "insert into `u_join_room_user` (`room_id`, `room_no`, `user_id`, `status`, `game_type`, `create_date`) values(?,?,?,?,?,?)";
		int i = DbUtil.getInstance().executeUpdate(sql, new Object[] { roomUser.getRoomId(), roomUser.getRoomNo(), roomUser.getUserId(), 2, GameTypeEnum.NIU_NIU.getType(), new Date() });
		return i;
	}

	/**
	 * 检查牛牛用户是否正在游戏中
	 * 
	 * @param userId
	 * @return
	 */
	public long checkNnUserIsJoinRoom(long userId) {
		String sql = "SELECT COUNT(1)>0 FROM u_join_room_user t WHERE t.user_id=?";
		long i = DbUtil.getInstance().getValue(sql, new Object[] { userId });
		return i;
	}

	/**
	 * 关闭房间
	 * 
	 * @param roomId
	 * @return
	 */
	public boolean closeRoom(long roomId) {
		String sql = "UPDATE nn_room t SET t.room_status=2,t.update_date=NOW() WHERE t.id=?";
		long i = DbUtil.getInstance().executeUpdate(sql, new Object[] { roomId });
		return i > 0;
	}

	public static void main(String[] args) {
		List<DbVo> list = new ArrayList<DbVo>();
		DbVo db = new DbVo();
		db.setUserId(1);
		db.setWinGold(1);

		GoldRecord goldRecord = new GoldRecord();
		goldRecord.setUserId(1l);
		goldRecord.setBindId(1l);
		goldRecord.setCreateDate(new Timestamp(System.currentTimeMillis()));
		db.setGoldRecord(goldRecord);

		UserCostRecord userCostRecord = new UserCostRecord();
		userCostRecord.setRoomId(1l);
		userCostRecord.setOrderNo("1234");
		userCostRecord.setCreateDate(new Date());
		db.setUserCostRecord(userCostRecord);

		NnRoomMatchUser nnRoomMatchUser = new NnRoomMatchUser();
		nnRoomMatchUser.setBaseGold(10);
		nnRoomMatchUser.setCostGold(1);
		nnRoomMatchUser.setCreateDate(new Date());
		db.setNnRoomMatchUser(nnRoomMatchUser);

		UserMatchRecord userMatchRecord = new UserMatchRecord();
		userMatchRecord.setBindId(1l);
		userMatchRecord.setGameType(1);
		userMatchRecord.setCreateDate(new Date());
		db.setUserMatchRecord(userMatchRecord);
		list.add(db);
		NnManagerDao.instance().addDb(list);
	}
}
