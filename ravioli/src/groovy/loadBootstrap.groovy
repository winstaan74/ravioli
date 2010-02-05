// code to load bootstrap
// use when running console.
l = [getAttribute : {_ -> ctx}]
b = new BootStrap()
b.searchableService = ctx.searchableService
b.regParserService = ctx.regParserService
b.init(l)