package com.fansauchiwa.data

/**
 * 開発版のホームにだけ出すテンプレートの候補。
 * 渡すのは `app/src/debug` の TemplateCandidateModule だけなので、release と beta には候補が入らない。
 */
class TemplateCandidates(val templates: List<Template>)
