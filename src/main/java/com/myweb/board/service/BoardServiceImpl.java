package com.myweb.board.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import com.myweb.board.model.BoardDAO;
import com.myweb.board.model.BoardDTO;
import com.myweb.board.model.BoardMapper;
import com.myweb.util.mybatis.MybatisUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class BoardServiceImpl implements BoardService {

	// 멤버변수에 세션팩토리 하나 선언
	private SqlSessionFactory sqlSessionFactory = MybatisUtil.getSqlSessionFactory();

	@Override
	public void regist(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 사용자가 입력한 값을 받음
		String writer = request.getParameter("writer");
		String title = request.getParameter("title");
		String content = request.getParameter("content");

		BoardDTO dto = new BoardDTO(0, writer, title, content, null, 0);

		SqlSession sql = sqlSessionFactory.openSession(true);
		BoardMapper board = sql.getMapper(BoardMapper.class);
		int result = board.regist(dto);
		System.out.println("성공여부: " + result);

		// 목록화면을 redirect 태워서 나간다.
		response.sendRedirect("list.board");

		/*
		 * //DAO객체생성 BoardDAO dao = BoardDAO.getInstance(); dao.regist(writer, title,
		 * content);
		 * 
		 * //목록화면으로 이동 >> 틀림 //목록화면을 redirect 태워서 나감...
		 * response.sendRedirect("list.board");
		 */

	}

	@Override
	public void getList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		/*
		 * //DAO생성 BoardDAO dao = BoardDAO.getInstance(); //목록조회 ArrayList<BoardDTO>
		 * list = dao.getList();
		 * 
		 * //request에 저장하고 forward로 나감 request.setAttribute("list", list);
		 * request.getRequestDispatcher("board_list.jsp").forward(request, response);
		 */

		SqlSession sql = sqlSessionFactory.openSession(); // 커넥션 객체
		BoardMapper board = sql.getMapper(BoardMapper.class); // 호출 시킬 인터페이스명 작성

//		 String now = board.now();
//		 System.out.println("sql문 잘 실행됨: "+now);

		ArrayList<BoardDTO> list = board.getList();

		sql.close(); // 마이바티스 세션 종료

		// request저장 포워드
		request.setAttribute("list", list);
		request.getRequestDispatcher("board_list.jsp").forward(request, response);
	}

	@Override
	public void getContent(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// a링크로 넘어 온 bno값을 받음
		String bno = request.getParameter("bno");

		// 마이바티스 실행
		SqlSession sql = sqlSessionFactory.openSession(true);
		BoardMapper mapper = sql.getMapper(BoardMapper.class); // 실행시키고 싶은 인터페이스명

		// 조회수
		mapper.increaseHit(bno);  // 조회수 증가
		BoardDTO dto = mapper.getContent(bno); // 결과반환
		sql.close(); // 마이바티스 세션 종료

		// dto를 request에 담고 forward 화면으로 이동
		request.setAttribute("dto", dto);
		request.getRequestDispatcher("board_content.jsp").forward(request, response);

	}

	@Override
	public void modify(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// a링크로 넘어 온 bno값을 받음
		String bno = request.getParameter("bno");

		// 마이바티스 실행
		SqlSession sql = sqlSessionFactory.openSession(true);
		BoardMapper mapper = sql.getMapper(BoardMapper.class); // 실행시키고 싶은 인터페이스명
		BoardDTO dto = mapper.getContent(bno); // 결과반환

		sql.close(); // 마이바티스 세션 종료

		// dto를 request에 답고 forward화면으로 이동
		request.setAttribute("dto", dto);
		request.getRequestDispatcher("board_modify.jsp").forward(request, response);

	}

	@Override
	public void update(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String bno = request.getParameter("bno");
		String title = request.getParameter("title");
		String content = request.getParameter("content");

		BoardDTO dto = new BoardDTO();
		dto.setBno(Integer.parseInt(bno));
		dto.setTitle(title);
		dto.setContent(content);

		// 마이바티스 실행
		SqlSession sql = sqlSessionFactory.openSession(true);
		BoardMapper mapper = sql.getMapper(BoardMapper.class);

		int result = mapper.update(dto);
		sql.close();

		if (result == 1) {// 업데이트 성공
			response.setContentType("text/html; charset=UTF-8;");
			PrintWriter out = response.getWriter();

			out.println("<script>");
			out.println("alert('정상 처리 되었따.');");
			out.println("location.href='list.board';");
			out.println("</script>");
		} else {// 업데이트 실패

			// 다시 수정화면으로

			response.sendRedirect("getContent.board?bno=" + bno); // content는 bno를 필요로 한다.

		}

	}

	@Override
	public void delete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*
		 * 1. 화면에서 전달되는 bno값을 얻음 
		 * 2. mapper에 <delete>태그를 추가한다. 
		 * 		-insert, update의 적용 방식이랑 똑같다. 
		 * 3. 서비스 영역에서는 bno를 이용해서 삭제를 진행하고, 
		 * 		삭제 후에는 목록페이지로 이동한다.
		 */

		String bno = request.getParameter("bno");
		
		BoardDTO dto = new BoardDTO();
		dto.setBno(Integer.parseInt(bno));
		
		SqlSession sql = sqlSessionFactory.openSession(true);
		BoardMapper mapper = sql.getMapper(BoardMapper.class);
		
		int result = mapper.delete(dto);
		sql.close();

		if(result==1){
			 response.setContentType("text/html; charset=UTF-8"); 
			 PrintWriter out = response.getWriter();
			  
			 out.println("<script>"); 
			 out.println("alert('삭제성공');");
			 out.println("location.href='list.board';"); 
			 out.println("</script>");
		} else {
			response.sendRedirect("getContent.board?bno=" + bno);
		}

	}

}
