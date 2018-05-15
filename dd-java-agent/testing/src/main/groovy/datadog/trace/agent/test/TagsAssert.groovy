package datadog.trace.agent.test

import datadog.opentracing.DDSpan

class TagsAssert {
  private final Map<String, Object> tags
  private final Set<String> assertedTags = new TreeSet<>()

  private TagsAssert(DDSpan span) {
    this.tags = new TreeMap(span.tags)
  }

  static TagsAssert assertTags(DDSpan span,
                               @DelegatesTo(value = TagsAssert, strategy = Closure.DELEGATE_FIRST) Closure spec) {
    def asserter = new TagsAssert(span)
    def clone = (Closure) spec.clone()
    clone.delegate = asserter
    clone.resolveStrategy = Closure.DELEGATE_FIRST
    clone(asserter)
    asserter.assertTracesAllVerified()
    asserter
  }

  def defaultTags() {
    assertedTags.add("thread.name")
    assertedTags.add("thread.id")

    tags["thread.name"] != null
    tags["thread.id"] != null
  }

  def errorTags(Class<Throwable> errorType) {
    errorTags(errorType, null)
  }

  def errorTags(Class<Throwable> errorType, String message) {
    assertedTags.add("error")
    assertedTags.add("error.type")
    assertedTags.add("error.stack")

    if (message != null) {
      assertedTags.add("error.msg")
      tags["error.msg"] == message
    }

    tags["error"] == true
    tags["error.type"] == errorType
    tags["error.stack"] instanceof String
  }

  def methodMissing(String name, args) {
    if (args.length > 1) {
      throw new IllegalArgumentException(args)
    }
    assertedTags.add(name)
    assert tags[name] == args[0]
  }

  void assertTracesAllVerified() {
    assert tags.keySet() == assertedTags
  }
}