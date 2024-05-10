package com.chasion.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {

    @Pointcut("execution(* com.chasion.community.service.*.*(..))")
    public void pointCut(){

    }


    @Before("pointCut()")
    public void before(){
        System.out.println("AlphaAspect.before()");
    }

    @After("pointCut()")
    public void after(){
        System.out.println("AlphaAspect.after()");
    }

    @AfterReturning("pointCut()")
    public void afterReturning(){
        System.out.println("AlphaAspect.afterReturning()");
    }

    @AfterThrowing("pointCut()")
    public void afterThrowing(){
        System.out.println("AlphaAspect.afterThrowing()");
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("around begin");
        Object proceed = joinPoint.proceed();
        System.out.println("around end");
        return proceed;
    }
}
