package com.nachoverdon.mongolia.filters;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterParameters {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
}
