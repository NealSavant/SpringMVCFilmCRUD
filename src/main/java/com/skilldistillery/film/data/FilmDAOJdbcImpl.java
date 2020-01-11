
package com.skilldistillery.film.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.skilldistillery.film.entities.Actor;
import com.skilldistillery.film.entities.Film;

public class FilmDAOJdbcImpl implements FilmDAO {

	private final String url = "jdbc:mysql://localhost:3306/sdvid?useSSL=false";
	private final String user = "student";
	private final String pass = "student";
	private final String sql = "Select film.id, film.title,  film.description, film.release_year, film.language_id, film.rental_duration, film.rental_rate, film.length, film.replacement_cost, film.rating, film.special_features, language.name from film join language on film.language_id = language.id";

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private PreparedStatement preparedAndBindStatementById(Connection conn, int id, String sql) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setInt(1, id);
		return stmt;
	}

	private PreparedStatement preparedAndBindStatementByKeyword(Connection conn, String word, String sql)
			throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(sql);

		stmt.setString(1, "%" + word + "%");
		stmt.setString(2, "%" + word + "%");
		return stmt;
	}

	@Override
	public Film findFilmById(int filmId) {
		Film film = null;
		String sql = this.sql + " where film.id = ?";
		try (Connection conn = DriverManager.getConnection(url, user, pass);
				PreparedStatement stmt = preparedAndBindStatementById(conn, filmId, sql);
				ResultSet rst = stmt.executeQuery();) {
			if (rst.next()) {
				film = new Film(rst.getInt("film.id"), rst.getString("film.title"), rst.getString("film.description"),
						rst.getInt("film.release_year"), rst.getInt("film.language_id"),
						rst.getInt("film.rental_duration"), rst.getDouble("film.rental_rate"),
						rst.getInt("film.length"), rst.getDouble("film.replacement_cost"), rst.getString("film.rating"),
						rst.getString("film.special_features"), rst.getString("language.name"),
						findActorsByFilmId(filmId));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return film;
	}

	@Override
	public List<Film> findFilmByKeyword(String filmKeyword) {
		List<Film> films = new ArrayList<Film>();
		Film film = null;
		String sql = this.sql + " where film.title like ? or film.description like ?";
		try (Connection conn = DriverManager.getConnection(url, user, pass);
				PreparedStatement stmt = preparedAndBindStatementByKeyword(conn, filmKeyword, sql);
				ResultSet rst = stmt.executeQuery();) {
			while (rst.next()) {
				film = new Film(rst.getInt("film.id"), rst.getString("film.title"), rst.getString("film.description"),
						rst.getInt("film.release_year"), rst.getInt("film.language_id"),
						rst.getInt("film.rental_duration"), rst.getDouble("film.rental_rate"),
						rst.getInt("film.length"), rst.getDouble("film.replacement_cost"), rst.getString("film.rating"),
						rst.getString("film.special_features"), rst.getString("language.name"),
						findActorsByFilmId(rst.getInt("film.id")));
				films.add(film);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return films;
	}

	@Override
	public Actor findActorById(int actorId) {
		Actor actor = null;
		String sql = "Select actor.id, actor.first_name, actor.last_name from actor where actor.id = ?";
		try (Connection conn = DriverManager.getConnection(url, user, pass);
				PreparedStatement stmt = preparedAndBindStatementById(conn, actorId, sql);
				ResultSet rst = stmt.executeQuery();) {
			if (rst.next()) {
				actor = new Actor(rst.getInt("actor.id"), rst.getString("actor.first_name"),
						rst.getString("actor.last_name"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return actor;
	}

	@Override
	public List<Actor> findActorsByFilmId(int filmId) {
		List<Actor> actors = new ArrayList<Actor>();
		Actor actor = null;
		String sql = "Select actor.id, actor.first_name, actor.last_name from actor join film_actor on actor.id = film_actor.actor_id where film_actor.film_id = ?";
		try (Connection conn = DriverManager.getConnection(url, user, pass);
				PreparedStatement stmt = preparedAndBindStatementById(conn, filmId, sql);
				ResultSet rst = stmt.executeQuery();) {
			while (rst.next()) {
				actor = new Actor(rst.getInt("actor.id"), rst.getString("actor.first_name"),
						rst.getString("actor.last_name"));
				actors.add(actor);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return actors;
	}

	@Override
	public Film addFilm(Film film) {
		String sql = "INSERT INTO film(title, description, release_year, language_id, rental_duration, rental_rate, length, replacement_cost, rating, special_features) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DriverManager.getConnection(this.url, this.user, this.pass);
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, film.getTitle());
			stmt.setString(2, film.getDescription());
			stmt.setInt(3, film.getReleaseYear());
			stmt.setInt(4, film.getLanguageId());
			stmt.setInt(5, film.getRentalDuration());
			stmt.setDouble(6, film.getRentalRate());
			stmt.setInt(7, film.getLength());
			stmt.setDouble(8, film.getReplacementCost());
			stmt.setString(9, film.getRating());
			stmt.setString(10, film.getSpecialFeatures());
			int updateCount = stmt.executeUpdate();
			if (updateCount == 1) {
				rs = stmt.getGeneratedKeys();
				if (rs.next()) {
					film.setId(rs.getInt(1));
				} else {
					throw new SQLException();
				}
			} else {
				throw new SQLException();
			}
			conn.commit();
		} catch (SQLException e) {
			film = null;
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}
		return film;
	}

	@Override
	public boolean deleteFilm(Film film) {
		String sql = "Delete FROM film where id = ?";
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DriverManager.getConnection(this.url, this.user, this.pass);
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, film.getId());
			int updateCount = stmt.executeUpdate();
			if (updateCount != 1) {
				throw new SQLException();
			}
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public boolean updateFilm(Film film) {
		String sql = "UPDATE film SET title = ?, description = ?, release_year = ?, language_id = ?, rental_duration = ?, rental_rate = ?, length = ?, replacement_cost = ?, rating = ?, special_features = ? where id = ?";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DriverManager.getConnection(this.url, this.user, this.pass);
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, film.getTitle());
			stmt.setString(2, film.getDescription());
			stmt.setInt(3, film.getReleaseYear());
			stmt.setInt(4, film.getLanguageId());
			stmt.setInt(5, film.getRentalDuration());
			stmt.setDouble(6, film.getRentalRate());
			stmt.setInt(7, film.getLength());
			stmt.setDouble(8, film.getReplacementCost());
			stmt.setString(9, film.getRating());
			stmt.setString(10, film.getSpecialFeatures());
			int updateCount = stmt.executeUpdate();
			if (updateCount != 1) {
				throw new SQLException();
			}
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}
		return true;
	}
}
