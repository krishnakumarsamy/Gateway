package com.community.gateway.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.community.gateway.dto.OperatorDTO;
import com.community.gateway.jwt.config.JwtUtils;
import com.community.gateway.jwt.config.UserDetailsImpl;
import com.community.gateway.jwt.response.JWTResponse;
import com.community.gateway.logical.OperatorLogical;

@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/gateway")
public class LoginController {

	@Autowired
	private OperatorLogical loginL;
	@Autowired
	AuthenticationManager authenticationManager;
	@Autowired
	JwtUtils jwtUtils;

	@PostMapping("/authenticate")
	public ResponseEntity<JWTResponse> login(@Valid @RequestBody OperatorDTO operatorRequest) {

		OperatorDTO operator = null;
		String jwt = null;
		String role = null ;
		try {
			operator = loginL.findByMobile(operatorRequest.getMobileNumber());

			System.out.println(
					" Operator login :" + operatorRequest.getMobileNumber() + "  ::: " + operatorRequest.getPassword());
			if (operatorRequest.getPassword().equals(operator.getPassword())) {
				Authentication authentication = authenticationManager
						.authenticate(new UsernamePasswordAuthenticationToken(
								operator.getOperator_Details().getOperatorName() + "-" + operator.getMobileNumber(),
								operator.getPassword()));

				SecurityContextHolder.getContext().setAuthentication(authentication);

				jwt = jwtUtils.generateJwtToken(authentication);
				UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
				List<String>roles= userDetails.getAuthorities().stream().map(item -> item.getAuthority())
						.collect(Collectors.toList());
				role=roles.get(0);
			}
		} catch (com.community.gateway.exception.ResourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Login Error Operator Not found");
			return ResponseEntity
					.badRequest()
					.body(new JWTResponse("Error: Login failed"));
			
		}

		return ResponseEntity.ok(
				new JWTResponse(jwt, operator.getId(), operator.getOperator_Details().getOperatorName(), operator.getMobileNumber(), role,"Success"));
			}

}
