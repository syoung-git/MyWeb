package com.myweb.user.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.myweb.user.model.UserDAO;
import com.myweb.util.JdbcUtil;

public class UserDAO {
	
	//DAO는 불필요하게 여러 개 만들 필요가 없으므로 
	//객체가 1개만 생성되도록 singleton형식으로 설계
	
	//1. 나 자신의 객체를 1개만 생성하고, private을 붙임
	private static UserDAO instance = new UserDAO();
	
	//2. 직접 객체를 생성할 수 없도록 생성자에도 private를 붙임
	private UserDAO(){
		
		//커넥션풀에 사용할 객체를 얻어 옴
		try {
			InitialContext init = new InitialContext();//시작설정객체
			
			ds = (DataSource)init.lookup("java:comp/env/jdbc/oracle");
			
		} catch (Exception e) {
			System.out.println("커넥션 풀 에러");
		}
		
	}
	//3. 객체 생성을 요구할 때 getter메서드를 사용해서 1번의 객체를 반환
	public static UserDAO getInstance() {
		return instance;
	}
	
	////////////////////////////
	//커넥션 풀 객체정보
	private DataSource ds;
	
	//아이디 중복검사
	public int findUser(String id) {
			int result =0;
			
			String sql = "SELECT * FROM USERS WHERE ID = ?";
			
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			
			try {
				
				conn = ds.getConnection();
				pstmt=conn.prepareStatement(sql);
				pstmt.setString(1, id);
				rs=pstmt.executeQuery();
				
				if(rs.next()) { //다음 행이 있으면 trud >> 유저가 있다는 뜻
					result = 1;//유저가 있다는 
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				JdbcUtil.close(conn, pstmt, rs);
			}
			
			
			return result;
	}
	
	//회원가입 메서드
	public void insertUser(String id, 
						 String pw,
						 String name,
						 String email,
						 String gender) { 
		String sql ="INSERT INTO USERS(ID, PW, NAME, EMAIL, GENDER) VALUES (?,?,?,?,?)";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		//ResultSet은 필요없음. SELECT문이 없기 때문
		
		try {
			conn = ds.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, id );
			pstmt.setString(2, pw );
			pstmt.setString(3, name );
			pstmt.setString(4, email );
			pstmt.setString(5, gender );
			
			pstmt.executeUpdate(); //i, u, d 구문은 executeUpdate();로 실행
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.close(conn, pstmt, null);
		}
		
	}
	
	//로그인
	public UserDTO login(String id, String pw) {
		UserDTO dto = null;
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT * FROM USERS WHERE ID=? AND PW=?";
		
		
		try {
			
			conn = ds.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, pw);
			
	
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				 String name = rs.getString("name");
				 String gender = rs.getString("gender");
				 String email = rs.getString("email");
				
				dto = new UserDTO(id, null, name, email, gender, null);
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.close(conn, pstmt, rs);
		}
		
		return dto;
	}
	
	//회원정보 조회 메서드
	public UserDTO getInfo(String id) {

		UserDTO dto = new UserDTO();
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		String sql = "SELECT * FROM USERS WHERE ID = ?";
		try {
			
			conn = ds.getConnection(); //DB연결하기 위한 객체
			pstmt = conn.prepareStatement(sql); //sql실행하기 위한 객체
			pstmt.setString(1,id);
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				//ORM(Object Relatioin Mapping)
				String pw = rs.getString("pw");
				String name = rs.getString("name");
				String email = rs.getString("email");
				String gender = rs.getString("gender");
				
				//setter - dto 안에 값이 저장됨
				dto.setId(id);
				dto.setName(name);
				dto.setEmail(email);
				dto.setGender(gender);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.close(conn, pstmt, rs);
		}
		return dto;
	}	
	
	//회원정보 수정 메서드 - 성공시 1반환, 실패시 0반환
	public int update(UserDTO dto) {
		
		int result = 0; //실패하면 0
		
		String sql = "UPDATE USERS SET PW=?, NAME=?, EMAIL=?, GENDER=? WHERE ID = ?";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			
			conn=ds.getConnection();
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, dto.getPw());
			pstmt.setString(2, dto.getName());
			pstmt.setString(3, dto.getEmail());
			pstmt.setString(4, dto.getGender());
			pstmt.setString(5, dto.getId());
			
			//반환 결과를 받고 싶으면, 0이면 실패 1이면 성공
			result = pstmt.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.close(conn, pstmt, null);
		}
		
		return result;
	}
	
	public void delete(String id, String pw) {
		
		String sql = "DELETE FROM USERS WHERE PW = ?";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			
			conn = ds.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pw);
			
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.close(conn, pstmt, null);
		}
	}
	
}
