## Testes unitários em JAVA: Domine JUnit, Mockito e TDD seguindo princípio FIRST (Fast, Isolated, Repeatable, Self-Validating, Timely)
> https://www.udemy.com/course/testes-unitarios-em-java/learn/lecture/6994632#overview

1. JUnit: Utilizando métodos do Assert, manipulado equals() da entidade para customizar comparação e assertSame, AssertThat e CoreMatchers, Rules e ErrorCollector, Tratamento de exceções, Before e After (+Class), FixMethodOrder
2. JUnit - Desafio: Refatorar o projeto anterior 1-JUnit para que a classe Locacao aceite um List de Filme em vez de um único objeto
3. TDD: Teste -> Código -> Refatoramento, Matcher personalizado, Builder e Chaining Method, Coverage report
4. Mocks: Mock e InjectMocks, when, thenReturn, verify, times, atLeast, atMost, atLeastOnce, verifyNoMoreInteractions, verifyZeroInteractions, never, any, ArgumentCaptor e SpyMock
5. PowerMock: PowerMockito.whenNew para mockar new Date(), Calendar.getInstance(). Mock de métodos, verifyPrivate e Whitebox.invokeMethod
6. Refatoracao: refatorado e eliminado PowerMock
7. TestesParalelos: testes paralelos com maven-surefire-plugin e classe personalizada implementando BlockJUnit4ClassRunner

Como testar: Cenário -> Ação -> Verificação
