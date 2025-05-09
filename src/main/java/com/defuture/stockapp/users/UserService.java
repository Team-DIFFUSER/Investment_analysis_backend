package com.defuture.stockapp.users;

import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserAccount create(UserDTO dto) {
		UserAccount user = new UserAccount();
		user.setUsername(dto.getUsername());
		user.setName(dto.getName());
		user.setEmail(dto.getEmail());
		user.setPassword(passwordEncoder.encode(dto.getPassword()));

		this.userRepository.save(user);
		return user;
	}

	public UserAccount findByUsername(String username) {
		Optional<UserAccount> user = userRepository.findByUsername(username);
		return user.orElse(null);
	}

	public boolean userExists(String id) {
		return userRepository.existsByUsername(id);
	}

	public boolean emailExists(String email) {
		return userRepository.existsByEmail(email);
	}

	public UserAccount createInvestmentProfile(String username, List<InvestmentResponseDTO> responses) {
		UserAccount user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		double totalScore = calculateTotalScore(responses);

		String ivestmentType = determineInvestmentType(totalScore);

		user.setInvestmentScore(totalScore);
		user.setInvestmentType(ivestmentType);

		userRepository.save(user);

		return user;
	}

	private double calculateTotalScore(List<InvestmentResponseDTO> responses) {
		double score = 0;
		for (InvestmentResponseDTO response : responses) {
			int questionId = response.getQuestionId();
			for (int option : response.getSelectedOption()) {
				score += getScore(questionId, option);
			}
		}
		return score;
	}

	private double getScore(int questionId, int selectedOption) {
		switch (questionId) {
		case 1:
			if (selectedOption == 1 || selectedOption == 2)
				return 12.5;
			if (selectedOption == 3)
				return 9.3;
			if (selectedOption == 4)
				return 6.2;
			if (selectedOption == 5)
				return 3.1;
			break;
		case 2:
			if (selectedOption == 1)
				return 3.1;
			if (selectedOption == 2)
				return 6.2;
			if (selectedOption == 3)
				return 9.3;
			if (selectedOption == 4)
				return 12.5;
			if (selectedOption == 5)
				return 15.6;
			break;
		case 3:
			if (selectedOption == 1)
				return 3.1;
			if (selectedOption == 2)
				return 6.2;
			if (selectedOption == 3)
				return 9.3;
			if (selectedOption == 4)
				return 12.5;
			if (selectedOption == 5)
				return 15.6;
			break;
		case 4:
			if (selectedOption == 1)
				return 3.1;
			if (selectedOption == 2)
				return 6.2;
			if (selectedOption == 3)
				return 9.3;
			if (selectedOption == 4)
				return 12.5;
			break;
		case 5:
			if (selectedOption == 1)
				return 15.6;
			if (selectedOption == 2)
				return 12.5;
			if (selectedOption == 3)
				return 9.3;
			if (selectedOption == 4)
				return 6.2;
			if (selectedOption == 5)
				return 3.1;
			break;
		case 6:
			if (selectedOption == 1)
				return 9.3;
			if (selectedOption == 2)
				return 6.2;
			if (selectedOption == 3)
				return 3.1;
			break;
		case 7:
			if (selectedOption == 1 || selectedOption == 2)
				return 6.2;
			if (selectedOption == 3)
				return 12.5;
			if (selectedOption == 4)
				return 18.7;
			break;
		}
		return 0;
	}

	private String determineInvestmentType(double totalScore) {
		if (totalScore <= 20) {
			return "안정형";
		} else if (totalScore <= 40) {
			return "안정추구형";
		} else if (totalScore <= 60) {
			return "위험중립형";
		} else if (totalScore <= 80) {
			return "적극투자형";
		} else {
			return "공격투자형";
		}
	}
}
